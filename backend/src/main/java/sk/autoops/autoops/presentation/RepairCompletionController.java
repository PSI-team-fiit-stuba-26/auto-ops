package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.RepairCompletionService;
import sk.autoops.autoops.dto.CompleteRepairRequest;
import sk.autoops.autoops.dto.CompleteRepairResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
public class RepairCompletionController {
    private final RepairCompletionService repairCompletionService;

    public RepairCompletionController(RepairCompletionService repairCompletionService) {
        this.repairCompletionService = repairCompletionService;
    }

    @PostMapping("/{repairOrderId}/complete")
    public CompleteRepairResponse completeRepair(@PathVariable UUID repairOrderId, @Valid @RequestBody CompleteRepairRequest request) {
        return repairCompletionService.completeRepair(repairOrderId, request);
    }
}
