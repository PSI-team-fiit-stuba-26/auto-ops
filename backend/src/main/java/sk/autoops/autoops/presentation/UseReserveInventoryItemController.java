package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.UseReserveInventoryItemService;
import sk.autoops.autoops.domain.InventoryItem;
import sk.autoops.autoops.domain.enums.InventoryStatus;
import sk.autoops.autoops.dto.CreateInventoryItemRequest;
import sk.autoops.autoops.dto.InventoryActionResponse;
import sk.autoops.autoops.dto.PagedResponse;
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

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable UUID id) {
        return useReserveInventoryItemService.findInventoryItem(null).stream()
                .filter(item -> id.equals(item.id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/page")
    public PagedResponse<InventoryItem> findInventoryItemPaged(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<InventoryItem> filtered = useReserveInventoryItemService.findInventoryItem(query).stream()
                .filter(item -> status == null || status == item.status)
                .toList();
        return PagedResponse.of(filtered, page, size);
    }

    @PostMapping
    public InventoryItem createItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        return useReserveInventoryItemService.createItem(request.code(), request.name(), request.count(), request.location(), request.price());
    }

    @PutMapping("/{id}")
    public InventoryItem updateItem(@PathVariable UUID id, @Valid @RequestBody UpdateInventoryItemRequest request) {
        return useReserveInventoryItemService.updateItem(id, request.name(), request.count(), request.location(), request.price());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        useReserveInventoryItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/use")
    public ResponseEntity<Void> useInventoryItem(@Valid @RequestBody UseInventoryItemRequest request) {
        useReserveInventoryItemService.useInventoryItem(request);
        InventoryItem item = useReserveInventoryItemService.findInventoryItem(null).stream()
                .filter(candidate -> candidate.id.equals(request.itemId()))
                .findFirst()
                .orElseThrow();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveInventoryItem(@Valid @RequestBody ReserveInventoryItemRequest request) {
        useReserveInventoryItemService.reserveInventoryItem(request);
        InventoryItem item = useReserveInventoryItemService.findInventoryItem(null).stream()
                .filter(candidate -> candidate.id.equals(request.itemId()))
                .findFirst()
                .orElseThrow();
        return ResponseEntity.ok().build();
    }
}
