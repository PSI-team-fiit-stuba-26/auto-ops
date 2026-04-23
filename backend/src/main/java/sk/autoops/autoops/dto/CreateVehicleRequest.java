package sk.autoops.autoops.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateVehicleRequest(
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{11,17}$", message = "must be a valid VIN")
        String vin,

        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-Z0-9 \\-]{2,15}$", message = "must be a valid license plate")
        String licensePlate,

        @NotBlank(message = "must not be blank")
        @Size(min = 1, max = 60)
        String brand,

        @NotBlank(message = "must not be blank")
        @Size(min = 1, max = 60)
        String model,

        @NotNull(message = "must not be null")
        @Min(value = 1900, message = "must not be earlier than 1900")
        @Max(value = 2100, message = "must not be later than 2100")
        Integer year,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must not be negative")
        Integer mileage,

        @NotNull(message = "must not be null")
        UUID customerId
) {
}
