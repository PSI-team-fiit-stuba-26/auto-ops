package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class VehicleRepository {
    private final ConcurrentHashMap<UUID, Vehicle> vehicles = new ConcurrentHashMap<>();

    public Vehicle save(Vehicle vehicle) {
        if (vehicle.id == null) {
            vehicle.id = UUID.randomUUID();
        }
        vehicles.put(vehicle.id, vehicle);
        return vehicle;
    }

    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(vehicles.get(id));
    }

    public Optional<Vehicle> findByVin(String vin) {
        if (vin == null || vin.isBlank()) {
            return Optional.empty();
        }
        return vehicles.values().stream()
                .filter(vehicle -> vin.equalsIgnoreCase(vehicle.vin))
                .findFirst();
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles.values());
    }
}
