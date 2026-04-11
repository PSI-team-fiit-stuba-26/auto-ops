package sk.autoops.autoops.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsageReservationRecord {
    public UUID id;
    public UUID repairJobId;
    public UUID itemId;
    public int amount;
    public String type;
    public LocalDateTime createdAt;

    public UsageReservationRecord() {
    }

    public UsageReservationRecord(UUID id, UUID repairJobId, UUID itemId, int amount, String type, LocalDateTime createdAt) {
        this.id = id;
        this.repairJobId = repairJobId;
        this.itemId = itemId;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }
}
