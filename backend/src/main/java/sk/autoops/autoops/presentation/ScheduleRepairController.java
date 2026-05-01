package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.RepairOrderService;
import sk.autoops.autoops.application.SchedulingService;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.dto.CreateRepairOrderRequest;
import sk.autoops.autoops.dto.ScheduleRepairResponse;

import java.util.List;

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

    @PostMapping
    public ScheduleRepairResponse confirmBooking(@Valid @RequestBody CreateRepairOrderRequest request) {
        return schedulingService.confirmBooking(request);
    }

    @PostMapping("/estimate")
    public int estimateDuration(@Valid @RequestBody CreateRepairOrderRequest request) {
        return schedulingService.estimateDuration(request);
    }
}
