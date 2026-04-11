package sk.autoops.autoops.external;

import org.springframework.stereotype.Component;
import sk.autoops.autoops.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentGateway {
    public PaymentStatus createPaymentRequest(UUID repairOrderId, BigDecimal amount) {
        return PaymentStatus.PENDING;
    }
}
