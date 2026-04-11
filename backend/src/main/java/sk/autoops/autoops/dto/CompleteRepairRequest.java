package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CompleteRepairRequest(
        @DecimalMin("0.0") double actualWorkHours,
        String note,
        @NotNull Boolean markAllTasksDone
) {
}
