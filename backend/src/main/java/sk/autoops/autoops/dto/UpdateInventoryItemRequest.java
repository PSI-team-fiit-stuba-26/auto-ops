package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInventoryItemRequest(
        @Size(min = 1, max = 120, message = "must be between 1 and 120 characters")
        String name,

        @PositiveOrZero(message = "must not be negative")
        Integer count,

        @Size(max = 60, message = "must not exceed 60 characters")
        String location,

        @DecimalMin(value = "0.0", inclusive = true, message = "must not be negative")
        @Digits(integer = 8, fraction = 2, message = "must have at most 8 whole digits and 2 decimals")
        BigDecimal price
) {
}
