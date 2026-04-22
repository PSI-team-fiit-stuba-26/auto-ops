package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.RepairTask;
import sk.autoops.autoops.domain.ServiceHistoryEntry;
import sk.autoops.autoops.domain.UsedPart;
import sk.autoops.autoops.domain.enums.PaymentStatus;
import sk.autoops.autoops.domain.enums.RepairFlag;
import sk.autoops.autoops.domain.enums.RepairOrderStatus;
import sk.autoops.autoops.domain.enums.RepairTaskStatus;
import sk.autoops.autoops.dto.CompleteRepairRequest;
import sk.autoops.autoops.dto.CompleteRepairResponse;
import sk.autoops.autoops.dto.InvoiceSummary;
import sk.autoops.autoops.external.BillingGateway;
import sk.autoops.autoops.external.PaymentGateway;
import sk.autoops.autoops.infrastructure.MechanicRepository;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;
import sk.autoops.autoops.infrastructure.ServiceHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.RoundingMode;

@Service
public class RepairCompletionService {
    private final RepairOrderRepository repairOrderRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final BillingGateway billingGateway;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.20");

    public RepairCompletionService(
            RepairOrderRepository repairOrderRepository,
            MechanicRepository mechanicRepository,
            ServiceHistoryRepository serviceHistoryRepository,
            BillingGateway billingGateway,
            PaymentGateway paymentGateway,
            NotificationService notificationService
    ) {
        this.repairOrderRepository = repairOrderRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceHistoryRepository = serviceHistoryRepository;
        this.billingGateway = billingGateway;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public CompleteRepairResponse completeRepair(UUID repairOrderId, CompleteRepairRequest request) {
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found"));

        if (Boolean.TRUE.equals(request.markAllTasksDone())) {
            repairOrder.tasks.forEach(task -> task.status = RepairTaskStatus.DONE);
        }
        if (!validateRepairCanBeClosed(repairOrder)) {
            throw new IllegalStateException("Repair has unfinished tasks");
        }

        repairOrder.actualWorkHours = request.actualWorkHours();
        repairOrder.completedAt = LocalDateTime.now();
        repairOrder.status = RepairOrderStatus.READY_FOR_PAYMENT;
        repairOrder.completionNote = request.note();

        InvoiceSummary invoice = createInvoice(repairOrder);
        PaymentStatus paymentStatus = paymentGateway.createPaymentRequest(repairOrder.id, invoice.totalAmount());
        InvoiceSummary payableInvoice = new InvoiceSummary(
                invoice.invoiceId(),
                invoice.invoiceNumber(),
                invoice.workAmount(),
                invoice.partsAmount(),
                invoice.subtotalAmount(),
                invoice.vatRate(),
                invoice.vatAmount(),
                invoice.totalAmount(),
                paymentStatus
        );
        billingGateway.publishInvoice(payableInvoice);
        notificationService.notifyRepairCompleted(repairOrder);

        List<String> warnings = new ArrayList<>();
        if (repairOrder.usedParts.isEmpty()) {
            warnings.add("No spare parts were recorded for this repair.");
        }
        if (repairOrder.maxPrice != null && payableInvoice.totalAmount().compareTo(repairOrder.maxPrice) > 0) {
            repairOrder.flags.add(RepairFlag.PRICE_LIMIT_EXCEEDED);
            warnings.add("Invoice exceeds customer's maximum expected price.");
        }
        repairOrderRepository.save(repairOrder);
        return new CompleteRepairResponse(repairOrder.id, repairOrder.status, repairOrder, payableInvoice, paymentStatus, null, warnings);
    }

    public void payInvoice(UUID repairOrderId) {
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found"));

        repairOrder.status = RepairOrderStatus.COMPLETED;
        createServiceHistory(repairOrder, repairOrder.completionNote);
        repairOrderRepository.save(repairOrder);
    }

    public boolean validateRepairCanBeClosed(RepairOrder repairOrder) {
        return repairOrder.tasks.stream().allMatch(task -> task.status == RepairTaskStatus.DONE);
    }

    public ServiceHistoryEntry createServiceHistory(RepairOrder repairOrder, String note) {
        ServiceHistoryEntry entry = new ServiceHistoryEntry();
        entry.repairOrderId = repairOrder.id;
        entry.vehicleId = repairOrder.vehicleId;
        entry.createdAt = LocalDateTime.now();
        entry.description = note == null || note.isBlank() ? repairOrder.problemDescription : note;
        entry.usedPartsSummary = repairOrder.usedParts.stream()
                .map(part -> part.partName + " x" + part.quantity)
                .reduce((left, right) -> left + ", " + right)
                .orElse("No parts used");
        entry.workHours = repairOrder.actualWorkHours;
        return serviceHistoryRepository.save(entry);
    }

    private InvoiceSummary createInvoice(RepairOrder repairOrder) {
        Mechanic mechanic = mechanicRepository.findById(repairOrder.mechanicId)
                .orElseThrow(() -> new IllegalStateException("Cannot calculate invoice: mechanic wage is missing"));

        if (mechanic.wage == null) {
            throw new IllegalStateException("Cannot calculate invoice: mechanic wage is missing");
        }

        BigDecimal workAmount = mechanic.wage.multiply(BigDecimal.valueOf(repairOrder.actualWorkHours));

        BigDecimal partsAmount = repairOrder.usedParts.stream()
                .map(UsedPart::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalAmount = workAmount.add(partsAmount);
        BigDecimal vatAmount = subtotalAmount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotalAmount.add(vatAmount);

        UUID invoiceId = UUID.randomUUID();
        String invoiceNumber = billingGateway.issueInvoiceNumber(repairOrder.id, totalAmount);

        return new InvoiceSummary(
                invoiceId,
                invoiceNumber,
                workAmount,
                partsAmount,
                subtotalAmount,
                VAT_RATE,
                vatAmount,
                totalAmount,
                PaymentStatus.NOT_CREATED
        );
    }
}
