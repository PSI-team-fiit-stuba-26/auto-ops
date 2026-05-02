package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.Reservation;
import sk.autoops.autoops.domain.UsageReservationRecord;
import sk.autoops.autoops.domain.enums.InventoryStatus;
import sk.autoops.autoops.infrastructure.InventoryItemRepository;
import sk.autoops.autoops.infrastructure.ReservationRepository;
import sk.autoops.autoops.infrastructure.UsageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryItemRepository inventoryItemRepository;
    private final UsageRepository usageRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository, UsageRepository usageRepository, ReservationRepository reservationRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.usageRepository = usageRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<InventoryItem> findInventoryItem(String query) {
        return inventoryItemRepository.search(query);
    }

    public InventoryItem findById(UUID itemId) {
        return inventoryItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
    }

    public UsageReservationRecord useInventoryItem(UUID repairJobId, UUID itemId, int amount) {
        InventoryItem item = findById(itemId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (item.count < amount) {
            throw new IllegalStateException("Insufficient stock. Available: " + item.count);
        }
        item.count -= amount;
        reservationRepository.findOpenReservation(repairJobId, itemId).ifPresent(reservation -> {
            reservation.consumed = true;
            item.reservedCount = Math.max(0, item.reservedCount - reservation.amount);
            reservationRepository.save(reservation);
        });
        refreshStatus(item);
        inventoryItemRepository.save(item);
        return addUsageRecord(repairJobId, itemId, amount);
    }

    public UsageReservationRecord reserveInventoryItem(UUID repairJobId, UUID itemId, int amount) {
        InventoryItem item = findById(itemId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        // int availableToReserve = item.count - item.reservedCount;
        // if (availableToReserve < amount) {
        //     throw new IllegalStateException("Insufficient stock to reserve. Available: " + availableToReserve);
        // }
        item.reservedCount += amount;
        refreshStatus(item);
        inventoryItemRepository.save(item);

        Reservation reservation = new Reservation();
        reservation.repairJobId = repairJobId;
        reservation.itemId = itemId;
        reservation.amount = amount;
        reservation.createdAt = LocalDateTime.now();
        reservationRepository.save(reservation);
        return addReservationRecord(repairJobId, itemId, amount);
    }

    public UsageReservationRecord addUsageRecord(UUID repairJobId, UUID itemId, int amount) {
        return usageRepository.save(new UsageReservationRecord(null, repairJobId, itemId, amount, "USE", LocalDateTime.now()));
    }

    public UsageReservationRecord addReservationRecord(UUID repairJobId, UUID itemId, int amount) {
        return usageRepository.save(new UsageReservationRecord(null, repairJobId, itemId, amount, "RESERVE", LocalDateTime.now()));
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
