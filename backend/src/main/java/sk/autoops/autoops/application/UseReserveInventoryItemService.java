package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.UsageReservationRecord;
import sk.autoops.autoops.domain.UsedPart;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UseReserveInventoryItemService {
    private final InventoryService inventoryService;
    private final RepairOrderRepository repairOrderRepository;

    public UseReserveInventoryItemService(InventoryService inventoryService, RepairOrderRepository repairOrderRepository) {
        this.inventoryService = inventoryService;
        this.repairOrderRepository = repairOrderRepository;
    }

    public List<InventoryItem> findInventoryItem(String query) {
        return inventoryService.findInventoryItem(query);
    }

    public UsageReservationRecord useInventoryItem(UUID repairJobId, UUID itemId, int amount) {
        UsageReservationRecord record = inventoryService.useInventoryItem(repairJobId, itemId, amount);
        RepairOrder order = repairOrderRepository.findById(repairJobId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found"));
        InventoryItem item = inventoryService.findById(itemId);
        order.usedParts.add(new UsedPart(UUID.randomUUID(), item.id, item.name, amount, item.price));
        repairOrderRepository.save(order);
        return record;
    }

    public UsageReservationRecord reserveInventoryItem(UUID repairJobId, UUID itemId, int amount) {
        repairOrderRepository.findById(repairJobId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found"));
        return inventoryService.reserveInventoryItem(repairJobId, itemId, amount);
    }
}
