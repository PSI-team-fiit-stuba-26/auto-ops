package sk.autoops.autoops.presentation;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.domain.ServiceHistoryEntry;
import sk.autoops.autoops.dto.VehicleServiceHistorySummary;
import sk.autoops.autoops.infrastructure.ServiceHistoryRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    public List<ServiceHistoryEntry> getAll(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<ServiceHistoryEntry> source = vehicleId != null
                ? serviceHistoryRepository.findByVehicleId(vehicleId)
                : serviceHistoryRepository.findAll();
        return source.stream()
                .filter(entry -> from == null || (entry.createdAt != null && !entry.createdAt.isBefore(from)))
                .filter(entry -> to == null || (entry.createdAt != null && !entry.createdAt.isAfter(to)))
                .sorted(Comparator.comparing((ServiceHistoryEntry entry) -> entry.createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceHistoryEntry> getOne(@PathVariable UUID id) {
        return serviceHistoryRepository.findAll().stream()
                .filter(entry -> id.equals(entry.id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public VehicleServiceHistorySummary summary(@RequestParam UUID vehicleId) {
        List<ServiceHistoryEntry> entries = serviceHistoryRepository.findByVehicleId(vehicleId);
        if (entries.isEmpty()) {
            return new VehicleServiceHistorySummary(vehicleId, 0, 0.0, null, null, List.of());
        }
        double totalHours = entries.stream().mapToDouble(entry -> entry.workHours).sum();
        LocalDateTime first = entries.stream()
                .map(entry -> entry.createdAt)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDateTime last = entries.stream()
                .map(entry -> entry.createdAt)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        List<ServiceHistoryEntry> sorted = entries.stream()
                .sorted(Comparator.comparing((ServiceHistoryEntry entry) -> entry.createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        return new VehicleServiceHistorySummary(vehicleId, entries.size(), totalHours, first, last, sorted);
    }
}
