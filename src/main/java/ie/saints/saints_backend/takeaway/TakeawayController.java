package ie.saints.saints_backend.takeaway;

import ie.saints.saints_backend.menu.MenuItem;
import ie.saints.saints_backend.menu.MenuItemRepository;
import ie.saints.saints_backend.reservation.BookingNotifier;
import ie.saints.saints_backend.reservation.OpeningHours;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/takeaway")
public class TakeawayController {

    private static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");
    private static final int MIN_LEAD_MINUTES = 30;

    private final TakeawayOrderRepository orders;
    private final MenuItemRepository menuItems;
    private final BookingNotifier notifier;

    public TakeawayController(TakeawayOrderRepository orders, MenuItemRepository menuItems,
                              BookingNotifier notifier) {
        this.orders = orders;
        this.menuItems = menuItems;
        this.notifier = notifier;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody TakeawayRequest req) {
        Map<String, String> errors = checkPickup(req.date(), req.time());
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // Merge duplicate lines, then price everything from the database
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (TakeawayRequest.Line line : req.items()) {
            wanted.merge(line.menuItemId(), line.quantity(), Integer::sum);
        }
        if (wanted.values().stream().anyMatch(q -> q > 20)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("items", "For more than 20 of one item please call us"));
        }

        Map<Long, MenuItem> found = menuItems.findAllById(wanted.keySet()).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        if (found.size() != wanted.size()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("items", "Some items are no longer available. Please refresh the page"));
        }

        TakeawayOrder order = new TakeawayOrder(req.name(), req.phone(), req.date(), req.time(), req.notes());
        wanted.forEach((id, qty) -> {
            MenuItem item = found.get(id);
            order.addLine(new OrderLine(item.getName(), qty, item.getPrice()));
        });
        TakeawayOrder saved = orders.save(order);

        // A notification failure must never lose the order
        try {
            notifier.notifyNewTakeaway(saved);
        } catch (Exception e) {
            System.err.println(">>> Notification failed for takeaway #" + saved.getId() + ": " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thank you, " + saved.getName()
                        + "! We have received your order and will call you to confirm."));
    }

    private Map<String, String> checkPickup(LocalDate date, LocalTime time) {
        Map<String, String> errors = new LinkedHashMap<>();
        Optional<OpeningHours.Hours> hours = OpeningHours.forDate(date);

        if (hours.isEmpty()) {
            errors.put("date", "We take takeaway orders on Friday, Saturday and Sunday only");
            return errors;
        }

        OpeningHours.Hours h = hours.get();
        String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        if (time.isBefore(h.open()) || time.isAfter(h.lastPickup())) {
            errors.put("time", "On " + day + " pickup is from " + h.open() + " to " + h.lastPickup());
        } else if (date.equals(LocalDate.now(DUBLIN))
                && time.isBefore(LocalTime.now(DUBLIN).plusMinutes(MIN_LEAD_MINUTES))) {
            errors.put("time", "Please allow at least " + MIN_LEAD_MINUTES + " minutes for preparation");
        }
        return errors;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}