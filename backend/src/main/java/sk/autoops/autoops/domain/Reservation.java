package sk.autoops.autoops.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {
    public UUID id;
    public UUID repairJobId;
    public UUID itemId;
    public int amount;
    public LocalDateTime createdAt;
    public boolean consumed;

    public Reservation() {
    }
}
