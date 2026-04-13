package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.ServiceHistoryEntry;
import sk.autoops.autoops.domain.Vehicle;
import sk.autoops.autoops.infrastructure.ServiceHistoryRepository;
import sk.autoops.autoops.infrastructure.VehicleRepository;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;

    public VehicleService(VehicleRepository vehicleRepository, ServiceHistoryRepository serviceHistoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.serviceHistoryRepository = serviceHistoryRepository;
    }

    public Vehicle findVehicle(UUID id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    public Vehicle findByVin(String vin) {
        return vehicleRepository.findByVin(vin).orElse(null);
    }

    public Vehicle registerVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<ServiceHistoryEntry> findRepairHistory(UUID vehicleId) {
        return serviceHistoryRepository.findAll().stream()
                .filter(entry -> vehicleId.equals(entry.vehicleId))
                .toList();
    }

    public void addRepairHistoryItem(ServiceHistoryEntry entry) {
        serviceHistoryRepository.save(entry);
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> findByCustomerId(UUID customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    public Vehicle updateVehicleFields(UUID id, String licensePlate, String brand, String model, Integer year, Integer mileage) {
        Vehicle vehicle = findVehicle(id);
        if (licensePlate != null && !licensePlate.isBlank()) vehicle.licensePlate = licensePlate;
        if (brand != null && !brand.isBlank()) vehicle.brand = brand;
        if (model != null && !model.isBlank()) vehicle.model = model;
        if (year != null && year > 0) vehicle.year = year;
        if (mileage != null && mileage >= 0) vehicle.mileage = mileage;
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(UUID id) {
        if (!vehicleRepository.delete(id)) {
            throw new IllegalArgumentException("Vehicle not found");
        }
    }
}
