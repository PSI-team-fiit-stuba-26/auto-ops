package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.RepairFlag;
import sk.autoops.autoops.domain.enums.RepairOrderStatus;
import sk.autoops.autoops.domain.enums.RepairTaskStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepairOrder {
    public UUID id;
    public RepairOrderStatus status = RepairOrderStatus.NEW;
    public double actualWorkHours;
    public LocalDateTime completedAt;
    public String problemDescription;
    public BigDecimal maxPrice;
    public LocalDateTime receivedDate;
    public LocalDateTime plannedStart;
    public LocalDateTime plannedCompletionDate;
    public UUID customerId;
    public UUID vehicleId;
    public UUID mechanicId;
    public List<RepairTask> tasks = new ArrayList<>();
    public List<UsedPart> usedParts = new ArrayList<>();
    public List<RepairFlag> flags = new ArrayList<>();
    public String completionNote;

    public RepairOrder() {
    }

    public boolean allTasksDone() {
        return tasks.stream().allMatch(task -> task.status == RepairTaskStatus.DONE);
    }
}
