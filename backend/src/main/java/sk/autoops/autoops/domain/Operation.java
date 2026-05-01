package sk.autoops.autoops.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Operation {
    public UUID id;
    public String name;
    public String description;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public boolean active = true;
    public BigDecimal currentPrice;
    public String limitations = "No limitations";
    public List<UUID> eligibleWorkers = new ArrayList<>();

    public Operation() {
    }
}
