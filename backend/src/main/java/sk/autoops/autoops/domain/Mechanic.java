package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mechanic {
    public UUID id;
    public String name;
    public List<MechanicSpecialty> specialties = new ArrayList<>();
    public String workLimitations;
    public BigDecimal wage;

    public Mechanic() {
    }

    public Mechanic(UUID id, String name, List<MechanicSpecialty> specialties, String workLimitations, BigDecimal wage) {
        this.id = id;
        this.name = name;
        this.specialties = specialties;
        this.workLimitations = workLimitations;
        this.wage = wage;
    }
}
