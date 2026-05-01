package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateOperationRequest(
        @NotNull @DecimalMin("0.0") BigDecimal newPrice,
        String description,
        String limitationsDesc,
        List<UUID> eligibleWorkers,
        @NotBlank String reason,
        UUID changedBy
) {
}
