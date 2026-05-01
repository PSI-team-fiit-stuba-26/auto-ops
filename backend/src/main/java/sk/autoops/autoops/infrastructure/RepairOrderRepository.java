package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.RepairOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RepairOrderRepository {
    private final ConcurrentHashMap<UUID, RepairOrder> repairOrders = new ConcurrentHashMap<>();

    public RepairOrder save(RepairOrder repairOrder) {
        if (repairOrder.id == null) {
            repairOrder.id = UUID.randomUUID();
        }
        repairOrders.put(repairOrder.id, repairOrder);
        return repairOrder;
    }

    public Optional<RepairOrder> findById(UUID id) {
        return Optional.ofNullable(repairOrders.get(id));
    }

    public List<RepairOrder> findAll() {
        return new ArrayList<>(repairOrders.values());
    }

    public List<RepairOrder> findMechanicAssignments(UUID mechanicId, LocalDateTime start, LocalDateTime end) {
        return repairOrders.values().stream()
                .filter(order -> mechanicId.equals(order.mechanicId))
                .filter(order -> order.plannedStart != null && order.plannedCompletionDate != null)
                .filter(order -> order.plannedStart.isBefore(end) && order.plannedCompletionDate.isAfter(start))
                .toList();
    }
}
