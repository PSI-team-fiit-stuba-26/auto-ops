package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.OperationService;
import sk.autoops.autoops.domain.Operation;
import sk.autoops.autoops.domain.PriceChangeLog;
import sk.autoops.autoops.dto.CreateOperationRequest;
import sk.autoops.autoops.dto.UpdateOperationRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
public class OperationController {
    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping
    public List<Operation> getAllOperations() {
        return operationService.getAllOperations();
    }

    @PostMapping
    public Operation createOperation(@Valid @RequestBody CreateOperationRequest request) {
        return operationService.createOperation(request);
    }

    @PutMapping("/{operationId}")
    public Operation updateOperation(@PathVariable UUID operationId, @Valid @RequestBody UpdateOperationRequest request) {
        return operationService.updateOperation(operationId, request);
    }

    @GetMapping("/price-changes")
    public List<PriceChangeLog> priceChanges() {
        return operationService.getPriceChangeLogs();
    }
}
