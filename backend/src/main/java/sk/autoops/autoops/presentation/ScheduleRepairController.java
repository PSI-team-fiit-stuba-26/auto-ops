package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.RepairOrderService;
import sk.autoops.autoops.application.SchedulingService;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.dto.CreateRepairOrderRequest;
import sk.autoops.autoops.dto.ScheduleRepairResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repair-orders")
public class ScheduleRepairController {
    private final SchedulingService schedulingService;
    private final RepairOrderService repairOrderService;

    public ScheduleRepairController(SchedulingService schedulingService, RepairOrderService repairOrderService) {
        this.schedulingService = schedulingService;
        this.repairOrderService = repairOrderService;
    }

    @GetMapping
    public List<RepairOrder> repairOrders() {
        return repairOrderService.findAll();
    }

    @GetMapping("/{id}")
    public RepairOrder getRepairOrder(@PathVariable UUID id) {
        return repairOrderService.getRepairOrder(id);
    }

    @PostMapping
    public ScheduleRepairResponse confirmBooking(@Valid @RequestBody CreateRepairOrderRequest request) {
        return schedulingService.confirmBooking(request);
    }

    @PostMapping("/estimate")
    public int estimateDuration(@Valid @RequestBody CreateRepairOrderRequest request) {
        return schedulingService.estimateDuration(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairOrder(@PathVariable UUID id) {
        repairOrderService.deleteRepairOrder(id);
        return ResponseEntity.noContent().build();
    }
}
