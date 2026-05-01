package sk.autoops.autoops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UseInventoryItemRequest(
        @NotNull UUID repairJobId,
        @NotNull UUID itemId,
        @Min(1) int amount
) {
}
