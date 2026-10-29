package ie.saints.saints_backend.reservation;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");

    private final ReservationRepository repository;
    private final BookingNotifier notifier;

    public ReservationController(ReservationRepository repository, BookingNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody ReservationRequest req) {
        Map<String, String> errors = checkOpeningHours(req.date(), req.time());
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Reservation saved = repository.save(new Reservation(
                req.name(), req.phone(), req.date(), req.time(),
                req.guests(), req.specialRequests()));

        // A notification failure must never lose the booking
        try {
            notifier.notifyNewBooking(saved);
        } catch (Exception e) {
            System.err.println(">>> Notification failed for booking #" + saved.getId() + ": " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thank you, " + saved.getName()
                        + "! We have received your request and will confirm shortly."));
    }

    private Map<String, String> checkOpeningHours(LocalDate date, LocalTime time) {
        Map<String, String> errors = new LinkedHashMap<>();
        Optional<OpeningHours.Hours> hours = OpeningHours.forDate(date);

        if (hours.isEmpty()) {
            errors.put("date", "We take bookings on Friday, Saturday and Sunday only");
            return errors;
        }

        OpeningHours.Hours h = hours.get();
        String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        if (time.isBefore(h.open()) || time.isAfter(h.lastBooking())) {
            errors.put("time", "On " + day + " we take bookings from " + h.open()
                    + " to " + h.lastBooking());
        } else if (date.equals(LocalDate.now(DUBLIN)) && time.isBefore(LocalTime.now(DUBLIN))) {
            errors.put("time", "That time has already passed");
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