package ie.saints.saints_backend.reservation;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opening-hours")
public class OpeningHoursController {

    // jsDay matches JavaScript getDay(): Sunday = 0 ... Saturday = 6
    public record DayHours(int jsDay, String day, String open, String close, String lastBooking) {}

    @GetMapping
    public List<DayHours> get() {
        return OpeningHours.HOURS.entrySet().stream()
                .map(e -> new DayHours(
                        e.getKey().getValue() % 7,
                        e.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        e.getValue().open().toString(),
                        e.getValue().close().toString(),
                        e.getValue().lastBooking().toString()))
                .toList();
    }
}