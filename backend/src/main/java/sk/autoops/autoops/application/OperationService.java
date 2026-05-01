package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Operation;
import sk.autoops.autoops.domain.PriceChangeLog;
import sk.autoops.autoops.dto.CreateOperationRequest;
import sk.autoops.autoops.dto.UpdateOperationRequest;
import sk.autoops.autoops.infrastructure.OperationRepository;
import sk.autoops.autoops.infrastructure.PriceChangeLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OperationService {
    private final OperationRepository operationRepository;
    private final PriceChangeLogRepository priceChangeLogRepository;
    private final NotificationService notificationService;

    public OperationService(OperationRepository operationRepository, PriceChangeLogRepository priceChangeLogRepository, NotificationService notificationService) {
        this.operationRepository = operationRepository;
        this.priceChangeLogRepository = priceChangeLogRepository;
        this.notificationService = notificationService;
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public Operation createOperation(CreateOperationRequest request) {
        Operation operation = new Operation();
        operation.name = request.name();
        operation.description = request.description();
        operation.currentPrice = request.price();
        operation.limitations = request.limitationsDesc() == null || request.limitationsDesc().isBlank()
                ? "No limitations"
                : request.limitationsDesc();
        operation.eligibleWorkers = request.eligibleWorkers() == null ? List.of() : request.eligibleWorkers();
        operation.createdAt = LocalDateTime.now();
        operation.updatedAt = operation.createdAt;
        return operationRepository.save(operation);
    }

    public Operation updateOperation(UUID operationId, UpdateOperationRequest request) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found"));
        if (!verifyChanges(request)) {
            throw new IllegalArgumentException("Invalid operation price change");
        }

        PriceChangeLog log = new PriceChangeLog();
        log.operationId = operation.id;
        log.oldPrice = operation.currentPrice;
        log.newPrice = request.newPrice();
        log.reason = request.reason();
        log.changedAt = LocalDateTime.now();
        log.changedBy = request.changedBy();
        priceChangeLogRepository.save(log);

        operation.currentPrice = request.newPrice();
        if (request.description() != null && !request.description().isBlank()) {
            operation.description = request.description();
        }
        if (request.limitationsDesc() != null && !request.limitationsDesc().isBlank()) {
            operation.limitations = request.limitationsDesc();
        }
        if (request.eligibleWorkers() != null) {
            operation.eligibleWorkers = request.eligibleWorkers();
        }
        operation.updatedAt = LocalDateTime.now();
        Operation saved = operationRepository.save(operation);
        notificationService.sendPriceChangeNotification(saved, log);
        return saved;
    }

    public boolean verifyChanges(UpdateOperationRequest request) {
        return request.newPrice() != null
                && request.newPrice().signum() >= 0
                && request.reason() != null
                && !request.reason().isBlank();
    }

    public List<PriceChangeLog> getPriceChangeLogs() {
        return priceChangeLogRepository.findAll();
    }
}
