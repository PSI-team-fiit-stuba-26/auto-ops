package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.InventoryItem;

import java.util.List;

public record InventoryActionResponse(
        InventoryItem item,
        List<String> warnings
) {
}
