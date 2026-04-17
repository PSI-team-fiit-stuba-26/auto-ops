package sk.autoops.autoops.presentation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.MechanicService;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.dto.CreateMechanicRequest;
import sk.autoops.autoops.dto.UpdateMechanicRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {
    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @GetMapping
    public List<Mechanic> getAll() {
        return mechanicService.findAll();
    }

    @GetMapping("/{id}")
    public Mechanic getById(@PathVariable UUID id) {
        return mechanicService.findById(id);
    }

    @PostMapping
    public Mechanic create(@Valid @RequestBody CreateMechanicRequest request) {
        return mechanicService.create(request.name(), request.specialties(), request.workLimitations(), request.wage());
    }

    @PutMapping("/{id}")
    public Mechanic update(@PathVariable UUID id, @RequestBody UpdateMechanicRequest request) {
        return mechanicService.update(id, request.name(), request.specialties(), request.workLimitations(), request.wage());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mechanicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
