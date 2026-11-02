package ie.saints.saints_backend.takeaway;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TakeawayRequest(
        @NotBlank(message = "Please enter your name")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Please enter your phone number")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "Phone number looks invalid")
        String phone,

        @NotNull(message = "Please choose a pickup date")
        @FutureOrPresent(message = "Date cannot be in the past")
        LocalDate date,

        @NotNull(message = "Please choose a pickup time")
        LocalTime time,

        @Size(max = 500, message = "Notes are too long")
        String notes,

        @NotEmpty(message = "Your order is empty")
        @Size(max = 30, message = "Too many different items")
        List<@Valid Line> items
) {
    public record Line(
            @NotNull Long menuItemId,
            @Min(1) @Max(20) int quantity
    ) {}
}