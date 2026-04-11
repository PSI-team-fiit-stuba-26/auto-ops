package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateRepairOrderRequest(
        UUID customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        UUID vehicleId,
        String vin,
        String licensePlate,
        String brand,
        String model,
        int year,
        int mileage,
        @NotBlank String problemDescription,
        @NotNull @DecimalMin("0.0") BigDecimal maxPrice,
        @NotNull UUID mechanicId,
        @NotNull LocalDateTime plannedStart,
        @NotNull LocalDateTime plannedCompletionDate,
        List<String> taskNames
) {
}
