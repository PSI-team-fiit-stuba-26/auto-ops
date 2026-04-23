package sk.autoops.autoops.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
        @Pattern(regexp = "^[A-Z0-9 \\-]{2,15}$", message = "must be a valid license plate")
        String licensePlate,

        @Size(min = 1, max = 60, message = "must be between 1 and 60 characters")
        String brand,

        @Size(min = 1, max = 60, message = "must be between 1 and 60 characters")
        String model,

        @Min(value = 1900, message = "must not be earlier than 1900")
        @Max(value = 2100, message = "must not be later than 2100")
        Integer year,

        @PositiveOrZero(message = "must not be negative")
        Integer mileage
) {
}
