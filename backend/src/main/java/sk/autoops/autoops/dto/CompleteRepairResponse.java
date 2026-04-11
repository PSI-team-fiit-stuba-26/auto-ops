package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.ServiceHistoryEntry;
import sk.autoops.autoops.domain.enums.PaymentStatus;
import sk.autoops.autoops.domain.enums.RepairOrderStatus;

import java.util.List;
import java.util.UUID;

public record CompleteRepairResponse(
        UUID orderId,
        RepairOrderStatus orderStatus,
        RepairOrder repairOrder,
        InvoiceSummary invoice,
        PaymentStatus paymentStatus,
        ServiceHistoryEntry serviceHistoryEntry,
        List<String> warnings
) {
}
