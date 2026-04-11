package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;
import sk.autoops.autoops.infrastructure.MechanicRepository;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;

import java.time.LocalDateTime;
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
