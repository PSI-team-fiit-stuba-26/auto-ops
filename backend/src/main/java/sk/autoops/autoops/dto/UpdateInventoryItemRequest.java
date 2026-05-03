package sk.autoops.autoops.dto;

import java.math.BigDecimal;

public record UpdateInventoryItemRequest(
        String name,
        Integer count,
        String location,
        BigDecimal price
) {
}
