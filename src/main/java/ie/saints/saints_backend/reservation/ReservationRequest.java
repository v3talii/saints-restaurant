package ie.saints.saints_backend.reservation;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
        @NotBlank(message = "Please enter your name")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Please enter your phone number")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "Phone number looks invalid")
        String phone,

        @NotNull(message = "Please choose a date")
        @FutureOrPresent(message = "Date cannot be in the past")
        LocalDate date,

        @NotNull(message = "Please choose a time")
        LocalTime time,

        @Min(value = 1, message = "At least 1 guest")
        @Max(value = 20, message = "For more than 20 guests please call us")
        int guests,

        @Size(max = 500, message = "Message is too long")
        String specialRequests
) {}