package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.InventoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryItemRepository {
    private final ConcurrentHashMap<UUID, InventoryItem> inventoryItems = new ConcurrentHashMap<>();

    public InventoryItem save(InventoryItem item) {
        if (item.id == null) {
            item.id = UUID.randomUUID();
        }
        inventoryItems.put(item.id, item);
        return item;
    }

    public Optional<InventoryItem> findById(UUID id) {
        return Optional.ofNullable(inventoryItems.get(id));
    }

    public List<InventoryItem> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String needle = query.toLowerCase();
        return inventoryItems.values().stream()
                .filter(item -> item.name.toLowerCase().contains(needle) || item.code.toLowerCase().contains(needle))
                .toList();
    }

    public List<InventoryItem> findAll() {
        return new ArrayList<>(inventoryItems.values());
    }
}
