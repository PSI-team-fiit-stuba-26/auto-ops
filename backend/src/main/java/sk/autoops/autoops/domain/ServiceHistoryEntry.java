package sk.autoops.autoops.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceHistoryEntry {
    public UUID id;
    public UUID repairOrderId;
    public UUID vehicleId;
    public LocalDateTime createdAt;
    public String description;
    public String usedPartsSummary;
    public double workHours;

    public ServiceHistoryEntry() {
    }
}
