package ie.saints.saints_backend.reservation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class OpeningHours {

    private static final int LAST_BOOKING_BEFORE_CLOSE_MIN = 60;

    public record Hours(LocalTime open, LocalTime close) {
        public LocalTime lastBooking() {
            return close.minusMinutes(LAST_BOOKING_BEFORE_CLOSE_MIN);
        }
    }

    // Edit opening hours here
    public static final Map<DayOfWeek, Hours> HOURS = new TreeMap<>(Map.of(
            DayOfWeek.FRIDAY, new Hours(LocalTime.of(17, 0), LocalTime.of(22, 0)),
            DayOfWeek.SATURDAY, new Hours(LocalTime.of(16, 0), LocalTime.of(22, 0)),
            DayOfWeek.SUNDAY, new Hours(LocalTime.of(13, 0), LocalTime.of(19, 0))
    ));

    public static Optional<Hours> forDate(LocalDate date) {
        return Optional.ofNullable(HOURS.get(date.getDayOfWeek()));
    }

    private OpeningHours() {}
}