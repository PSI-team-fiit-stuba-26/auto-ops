package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.UseReserveInventoryItemService;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.dto.CreateInventoryItemRequest;
import sk.autoops.autoops.dto.InventoryActionResponse;
import sk.autoops.autoops.dto.ReserveInventoryItemRequest;
import sk.autoops.autoops.dto.UpdateInventoryItemRequest;
import sk.autoops.autoops.dto.UseInventoryItemRequest;

import java.util.List;
import java.util.UUID;

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

    @PostMapping
    public InventoryItem createItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        return useReserveInventoryItemService.createItem(request.code(), request.name(), request.count(), request.location(), request.price());
    }

    @PutMapping("/{id}")
    public InventoryItem updateItem(@PathVariable UUID id, @RequestBody UpdateInventoryItemRequest request) {
        return useReserveInventoryItemService.updateItem(id, request.name(), request.count(), request.location(), request.price());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        useReserveInventoryItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
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
