package sk.autoops.autoops.dto;

import sk.autoops.autoops.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceSummary(
        UUID invoiceId,
        String invoiceNumber,
        BigDecimal workAmount,
        BigDecimal partsAmount,
        BigDecimal subTotalAmount,
        BigDecimal vatAmount,
        BigDecimal totalAmount,
        PaymentStatus status
) {
}
