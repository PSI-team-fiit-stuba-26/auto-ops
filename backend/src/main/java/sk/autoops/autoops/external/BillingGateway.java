package sk.autoops.autoops.external;

import org.springframework.stereotype.Component;
import sk.autoops.autoops.dto.InvoiceSummary;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BillingGateway {
    public String issueInvoiceNumber(UUID repairOrderId, BigDecimal totalAmount) {
        return "INV-" + repairOrderId.toString().substring(0, 8).toUpperCase();
    }

    public void publishInvoice(InvoiceSummary invoice) {
        // Simulates integration with accounting software in the EA external layer.
    }
}
