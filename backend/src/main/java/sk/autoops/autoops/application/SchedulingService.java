package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.dto.CreateRepairOrderRequest;
import sk.autoops.autoops.dto.ScheduleRepairResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulingService {
    private final MechanicService mechanicService;
    private final UseReserveInventoryItemService inventoryService;
    private final RepairOrderService repairOrderService;
    private final NotificationService notificationService;

    public SchedulingService(MechanicService mechanicService, UseReserveInventoryItemService inventoryService, RepairOrderService repairOrderService, NotificationService notificationService) {
        this.mechanicService = mechanicService;
        this.inventoryService = inventoryService;
        this.repairOrderService = repairOrderService;
        this.notificationService = notificationService;
    }

    public int estimateDuration(CreateRepairOrderRequest request) {
        int taskCount = request.taskNames() == null || request.taskNames().isEmpty() ? 3 : request.taskNames().size();
        return Math.max(2, taskCount * 2);
    }

    public boolean checkMechanicAvailability(CreateRepairOrderRequest request) {
        return mechanicService.checkMechanicSchedule(request.mechanicId(), request.plannedStart(), request.plannedCompletionDate());
    }

    public boolean checkPartsAvailability(CreateRepairOrderRequest request) {
        return inventoryService.findInventoryItem(null).stream().anyMatch(item -> item.count > item.reservedCount);
    }

    public ScheduleRepairResponse confirmBooking(CreateRepairOrderRequest request) {
        List<String> warnings = new ArrayList<>();
        boolean mechanicAvailable = checkMechanicAvailability(request);
        if (!mechanicAvailable) {
            throw new IllegalStateException("Selected mechanic is busy in the requested time window");
        }
        List<InventoryItem> availableParts = inventoryService.findInventoryItem(null).stream()
                .filter(item -> item.count > item.reservedCount)
                .toList();
        boolean partsAvailable = !availableParts.isEmpty();
        if (!partsAvailable) {
            warnings.add("No spare parts are currently available; order can still be scheduled and flagged.");
        }
        RepairOrder order = repairOrderService.createRepairOrder(request);
        String mechanicName = mechanicService.findById(order.mechanicId).name;
        notificationService.notifyMechanicAssigned(order, mechanicName);
        return new ScheduleRepairResponse(order, mechanicAvailable, partsAvailable, warnings);
    }
}
