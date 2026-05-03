package sk.autoops.autoops.presentation;

import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.domain.ServiceHistoryEntry;
import sk.autoops.autoops.infrastructure.ServiceHistoryRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-history")
public class ServiceHistoryController {
    private final ServiceHistoryRepository serviceHistoryRepository;

    public ServiceHistoryController(ServiceHistoryRepository serviceHistoryRepository) {
        this.serviceHistoryRepository = serviceHistoryRepository;
    }

    @GetMapping
    public List<ServiceHistoryEntry> getAll(@RequestParam(required = false) UUID vehicleId) {
        if (vehicleId != null) {
            return serviceHistoryRepository.findByVehicleId(vehicleId);
        }
        return serviceHistoryRepository.findAll();
    }
}
