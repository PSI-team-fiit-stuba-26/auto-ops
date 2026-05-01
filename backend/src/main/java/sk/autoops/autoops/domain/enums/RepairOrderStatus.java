package sk.autoops.autoops.domain.enums;

public enum RepairOrderStatus {
    NEW,
    SCHEDULED,
    IN_PROGRESS,
    WAITING_FOR_PARTS,
    READY_FOR_PAYMENT,
    COMPLETED,
    CANCELLED
}
