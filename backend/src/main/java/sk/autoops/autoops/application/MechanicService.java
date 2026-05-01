package sk.autoops.autoops.application;

import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MechanicService {
    List<Mechanic> findBySpecialtyTags(MechanicSpecialty specialty);

    boolean checkMechanicSchedule(UUID mechanicId, LocalDateTime start, LocalDateTime end);

    List<Mechanic> getAvailableMechanics(MechanicSpecialty specialty, LocalDateTime start, LocalDateTime end);
}
