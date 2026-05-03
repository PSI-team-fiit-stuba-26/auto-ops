package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.ServiceHistoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ServiceHistoryRepository {
    private final ConcurrentHashMap<UUID, ServiceHistoryEntry> entries = new ConcurrentHashMap<>();

    public ServiceHistoryEntry save(ServiceHistoryEntry entry) {
        if (entry.id == null) {
            entry.id = UUID.randomUUID();
        }
        entries.put(entry.id, entry);
        return entry;
    }

    public List<ServiceHistoryEntry> findAll() {
        return new ArrayList<>(entries.values());
    }

    public List<ServiceHistoryEntry> findByVehicleId(UUID vehicleId) {
        return entries.values().stream()
                .filter(e -> vehicleId.equals(e.vehicleId))
                .toList();
    }
}
