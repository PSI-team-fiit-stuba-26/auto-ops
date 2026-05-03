package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.math.BigDecimal;
import java.util.List;

public record UpdateMechanicRequest(
        String name,
        List<MechanicSpecialty> specialties,
        String workLimitations,
        BigDecimal wage
) {
}
