package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateInventoryItemRequest(
        @NotBlank String code,
        @NotBlank String name,
        @Min(0) int count,
        String location,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}
