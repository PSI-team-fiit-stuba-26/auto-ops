package sk.autoops.autoops.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class UsedPart {
    public UUID id;
    public UUID itemId;
    public String partName;
    public int quantity;
    public BigDecimal unitPrice;

    public UsedPart() {
    }

    public UsedPart(UUID id, UUID itemId, String partName, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.itemId = itemId;
        this.partName = partName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
