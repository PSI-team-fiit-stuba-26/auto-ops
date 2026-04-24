package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.ServiceHistoryEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VehicleServiceHistorySummary(
        UUID vehicleId,
        int totalEntries,
        double totalWorkHours,
        LocalDateTime firstServiceAt,
        LocalDateTime lastServiceAt,
        List<ServiceHistoryEntry> entries
) {
}
