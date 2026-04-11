package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MechanicRepository {
    private final ConcurrentHashMap<UUID, Mechanic> mechanics = new ConcurrentHashMap<>();

    public Mechanic save(Mechanic mechanic) {
        if (mechanic.id == null) {
            mechanic.id = UUID.randomUUID();
        }
        mechanics.put(mechanic.id, mechanic);
        return mechanic;
    }

    public Optional<Mechanic> findById(UUID id) {
        return Optional.ofNullable(mechanics.get(id));
    }

    public List<Mechanic> findBySpecialty(MechanicSpecialty specialty) {
        return mechanics.values().stream()
                .filter(mechanic -> specialty == null || mechanic.specialties.contains(specialty))
                .toList();
    }

    public List<Mechanic> findAll() {
        return new ArrayList<>(mechanics.values());
    }
}
