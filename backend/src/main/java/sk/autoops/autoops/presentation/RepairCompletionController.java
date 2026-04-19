package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.RepairCompletionService;
import sk.autoops.autoops.dto.CompleteRepairRequest;
import sk.autoops.autoops.dto.CompleteRepairResponse;
import sk.autoops.autoops.domain.enums.UserRole;

import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
@RequireRole({UserRole.ADMIN, UserRole.SERVICE_ADVISOR, UserRole.MECHANIC, UserRole.CUSTOMER})
public class RepairCompletionController {
    private final RepairCompletionService repairCompletionService;

    public RepairCompletionController(RepairCompletionService repairCompletionService) {
        this.repairCompletionService = repairCompletionService;
    }

    @PostMapping("/{repairOrderId}/complete")
    @RequireRole({UserRole.ADMIN, UserRole.SERVICE_ADVISOR, UserRole.MECHANIC})
    public CompleteRepairResponse completeRepair(@PathVariable UUID repairOrderId, @Valid @RequestBody CompleteRepairRequest request) {
        return repairCompletionService.completeRepair(repairOrderId, request);
    }

    @PostMapping("/{repairOrderId}/pay")
    @RequireRole({UserRole.ADMIN, UserRole.SERVICE_ADVISOR, UserRole.CUSTOMER})
    public ResponseEntity<Void> payInvoice(@PathVariable UUID repairOrderId) {
        repairCompletionService.payInvoice(repairOrderId);
        return ResponseEntity.ok().build();
    }
}
