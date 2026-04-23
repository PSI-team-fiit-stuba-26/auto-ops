package sk.autoops.autoops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.math.BigDecimal;
import java.util.List;

public record UpdateMechanicRequest(
        @Size(min = 2, max = 120, message = "must be between 2 and 120 characters")
        String name,

        List<MechanicSpecialty> specialties,

        @Size(max = 500, message = "must not exceed 500 characters")
        String workLimitations,

        @DecimalMin(value = "0.0", inclusive = true, message = "must not be negative")
        @Digits(integer = 8, fraction = 2, message = "must have at most 8 whole digits and 2 decimals")
        BigDecimal wage
) {
}
