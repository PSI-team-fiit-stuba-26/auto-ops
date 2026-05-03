package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.Reservation;
import sk.autoops.autoops.domain.UsageReservationRecord;
import sk.autoops.autoops.domain.UsedPart;
import sk.autoops.autoops.domain.enums.InventoryStatus;
import sk.autoops.autoops.dto.UseInventoryItemRequest;
import sk.autoops.autoops.dto.ReserveInventoryItemRequest;
import sk.autoops.autoops.infrastructure.InventoryItemRepository;
import sk.autoops.autoops.infrastructure.ReservationRepository;
import sk.autoops.autoops.infrastructure.UsageRepository;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UseReserveInventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final UsageRepository usageRepository;
    private final ReservationRepository reservationRepository;
    private final RepairOrderRepository repairOrderRepository;

    public UseReserveInventoryItemService(InventoryItemRepository inventoryItemRepository, UsageRepository usageRepository, ReservationRepository reservationRepository, RepairOrderRepository repairOrderRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.usageRepository = usageRepository;
        this.reservationRepository = reservationRepository;
        this.repairOrderRepository = repairOrderRepository;
    }

    public List<InventoryItem> findInventoryItem(String query) {
        return inventoryItemRepository.search(query);
    }

    public void useInventoryItem(UseInventoryItemRequest request) {
        var itemOpt = inventoryItemRepository.findById(request.itemId());
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid Id provided");
        }

        var item = itemOpt.get();
        if (request.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (item.count < request.amount()) {
            throw new IllegalStateException("Insufficient stock. Available: " + item.count);
        }
        item.count -= request.amount();
        refreshStatus(item);
        
        RepairOrder order = repairOrderRepository.findById(request.repairJobId())
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found"));
        order.usedParts.add(new UsedPart(UUID.randomUUID(), item.id, item.name, request.amount(), item.price));
        repairOrderRepository.save(order);
        addUsageRecord(request.repairJobId(), request.itemId(), request.amount());
        inventoryItemRepository.save(item);
    }

    public void reserveInventoryItem(ReserveInventoryItemRequest request) {
        var itemOpt = inventoryItemRepository.findById(request.itemId());
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid Id provided");
        }

        var item = itemOpt.get();
        if (request.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        item.reservedCount += request.amount();
        addReservationRecord(request.repairJobId(), request.itemId(), request.amount());
        inventoryItemRepository.save(item);
    }

    public void addUsageRecord(UUID repairJobId, UUID itemId, int amount) {
        usageRepository.save(new UsageReservationRecord(null, repairJobId, itemId, amount, "USE", LocalDateTime.now()));
    }

    public void addReservationRecord(UUID repairJobId, UUID itemId, int amount) {
        reservationRepository.save(new UsageReservationRecord(null, repairJobId, itemId, amount, "RESERVE", LocalDateTime.now()));
    }

    private void refreshStatus(InventoryItem item) {
        if (item.count <= 0) {
            item.status = InventoryStatus.OUT_OF_STOCK;
        } else if (item.reservedCount > 0) {
            item.status = InventoryStatus.RESERVED;
        } else if (item.count <= 2) {
            item.status = InventoryStatus.LOW_STOCK;
        } else {
            item.status = InventoryStatus.AVAILABLE;
        }
    }
}
