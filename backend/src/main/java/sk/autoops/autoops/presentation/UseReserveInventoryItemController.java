package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.UseReserveInventoryItemService;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.UsageReservationRecord;
import sk.autoops.autoops.dto.InventoryActionResponse;
import sk.autoops.autoops.dto.ReserveInventoryItemRequest;
import sk.autoops.autoops.dto.UseInventoryItemRequest;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class UseReserveInventoryItemController {
    private final UseReserveInventoryItemService useReserveInventoryItemService;

    public UseReserveInventoryItemController(UseReserveInventoryItemService useReserveInventoryItemService) {
        this.useReserveInventoryItemService = useReserveInventoryItemService;
    }

    @GetMapping
    public List<InventoryItem> findInventoryItem(@RequestParam(required = false) String query) {
        return useReserveInventoryItemService.findInventoryItem(query);
    }

    @PostMapping("/use")
    public InventoryActionResponse useInventoryItem(@Valid @RequestBody UseInventoryItemRequest request) {
        useReserveInventoryItemService.useInventoryItem(request);
        InventoryItem item = useReserveInventoryItemService.findInventoryItem(null).stream()
                .filter(candidate -> candidate.id.equals(request.itemId()))
                .findFirst()
                .orElseThrow();
        return new InventoryActionResponse(item, List.of());
    }

    @PostMapping("/reserve")
    public InventoryActionResponse reserveInventoryItem(@Valid @RequestBody ReserveInventoryItemRequest request) {
        useReserveInventoryItemService.reserveInventoryItem(request);
        InventoryItem item = useReserveInventoryItemService.findInventoryItem(null).stream()
                .filter(candidate -> candidate.id.equals(request.itemId()))
                .findFirst()
                .orElseThrow();
        return new InventoryActionResponse(item, List.of("Item reserved for repair order."));
    }
}
