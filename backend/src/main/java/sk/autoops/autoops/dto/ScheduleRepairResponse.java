package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.RepairOrder;

import java.util.List;

public record ScheduleRepairResponse(
        RepairOrder repairOrder,
        boolean mechanicAvailable,
        boolean partsAvailable,
        List<String> warnings
) {
}
