package sk.autoops.autoops.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PriceChangeLog {
    public UUID id;
    public UUID operationId;
    public BigDecimal oldPrice;
    public BigDecimal newPrice;
    public String reason;
    public LocalDateTime changedAt;
    public UUID changedBy;

    public PriceChangeLog() {
    }
}
