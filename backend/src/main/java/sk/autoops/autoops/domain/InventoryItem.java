package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.InventoryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class InventoryItem {
    public UUID id;
    public String code;
    public String name;
    public int count;
    public int reservedCount;
    public String location;
    public BigDecimal price;
    public LocalDate expectedDeliveryDate;
    public InventoryStatus status = InventoryStatus.AVAILABLE;

    public InventoryItem() {
    }

    public InventoryItem(UUID id, String code, String name, int count, String location, BigDecimal price) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.count = count;
        this.location = location;
        this.price = price;
    }
}
