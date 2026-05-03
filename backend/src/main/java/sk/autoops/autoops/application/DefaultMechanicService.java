package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;
import sk.autoops.autoops.infrastructure.MechanicRepository;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultMechanicService implements MechanicService {
    private final MechanicRepository mechanicRepository;
    private final RepairOrderRepository repairOrderRepository;

    public DefaultMechanicService(MechanicRepository mechanicRepository, RepairOrderRepository repairOrderRepository) {
        this.mechanicRepository = mechanicRepository;
        this.repairOrderRepository = repairOrderRepository;
    }

    @Override
    public List<Mechanic> findAll() {
        return mechanicRepository.findAll();
    }

    @Override
    public Mechanic findById(UUID id) {
        return mechanicRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Mechanic not found"));
    }

    @Override
    public Mechanic create(String name, List<MechanicSpecialty> specialties, String workLimitations, BigDecimal wage) {
        Mechanic mechanic = new Mechanic(
                null,
                name,
                specialties == null ? new ArrayList<>() : specialties,
                workLimitations == null || workLimitations.isBlank() ? "No limitations" : workLimitations,
                wage
        );
        return mechanicRepository.save(mechanic);
    }

    @Override
    public Mechanic update(UUID id, String name, List<MechanicSpecialty> specialties, String workLimitations, BigDecimal wage) {
        Mechanic mechanic = findById(id);
        if (name != null && !name.isBlank()) mechanic.name = name;
        if (specialties != null) mechanic.specialties = specialties;
        if (workLimitations != null && !workLimitations.isBlank()) mechanic.workLimitations = workLimitations;
        if (wage != null) mechanic.wage = wage;
        return mechanicRepository.save(mechanic);
    }

    @Override
    public void delete(UUID id) {
        if (!mechanicRepository.delete(id)) {
            throw new IllegalArgumentException("Mechanic not found");
        }
    }

    @Override
    public List<Mechanic> findBySpecialtyTags(MechanicSpecialty specialty) {
        return mechanicRepository.findBySpecialty(specialty);
    }

    @Override
    public boolean checkMechanicSchedule(UUID mechanicId, LocalDateTime start, LocalDateTime end) {
        return repairOrderRepository.findMechanicAssignments(mechanicId, start, end).isEmpty();
    }

    @Override
    public List<Mechanic> getAvailableMechanics(MechanicSpecialty specialty, LocalDateTime start, LocalDateTime end) {
        return findBySpecialtyTags(specialty).stream()
                .filter(mechanic -> checkMechanicSchedule(mechanic.id, start, end))
                .toList();
    }
}
