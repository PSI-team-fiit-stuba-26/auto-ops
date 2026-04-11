package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.RepairTaskStatus;

import java.util.UUID;

public class RepairTask {
    public UUID id;
    public String name;
    public RepairTaskStatus status;

    public RepairTask() {
    }

    public RepairTask(UUID id, String name, RepairTaskStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }
}
