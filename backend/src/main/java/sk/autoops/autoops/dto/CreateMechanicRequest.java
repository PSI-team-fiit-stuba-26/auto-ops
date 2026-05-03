package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.math.BigDecimal;
import java.util.List;

public record CreateMechanicRequest(
        @NotBlank String name,
        List<MechanicSpecialty> specialties,
        String workLimitations,
        @NotNull @DecimalMin("0.0") BigDecimal wage
) {
}
