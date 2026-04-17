package sk.autoops.autoops.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.CustomerService;
import sk.autoops.autoops.application.MechanicService;
import sk.autoops.autoops.application.VehicleService;
import sk.autoops.autoops.domain.Customer;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.Vehicle;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;
import sk.autoops.autoops.dto.UpdateCustomerRequest;
import sk.autoops.autoops.dto.UpdateVehicleRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final MechanicService mechanicService;

    public PlanningController(CustomerService customerService, VehicleService vehicleService, MechanicService mechanicService) {
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.mechanicService = mechanicService;
    }

    @GetMapping("/customers")
    public List<Customer> customers() {
        return customerService.findAll();
    }

    @GetMapping("/customers/search")
    public Customer checkCustomer(@RequestParam String email) {
        return customerService.findByEmail(email);
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable UUID id) {
        return customerService.findCustomer(id);
    }

    @PostMapping("/customers")
    public Customer newCustomer(@RequestBody Customer customer) {
        return customerService.registerCustomer(customer);
    }

    @PutMapping("/customers/{id}")
    public Customer updateCustomer(@PathVariable UUID id, @RequestBody UpdateCustomerRequest request) {
        return customerService.updateCustomer(id, request.name(), request.email(), request.phone());
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles")
    public List<Vehicle> vehicles(@RequestParam(required = false) UUID customerId) {
        if (customerId != null) {
            return vehicleService.findByCustomerId(customerId);
        }
        return vehicleService.findAll();
    }

    @GetMapping("/vehicles/search")
    public Vehicle checkVehicle(@RequestParam String vin) {
        return vehicleService.findByVin(vin);
    }

    @GetMapping("/vehicles/{id}")
    public Vehicle getVehicle(@PathVariable UUID id) {
        return vehicleService.findVehicle(id);
    }

    @PostMapping("/vehicles")
    public Vehicle newVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.registerVehicle(vehicle);
    }

    @PutMapping("/vehicles/{id}")
    public Vehicle updateVehicle(@PathVariable UUID id, @RequestBody UpdateVehicleRequest request) {
        return vehicleService.updateVehicleFields(id, request.licensePlate(), request.brand(), request.model(), request.year(), request.mileage());
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mechanics/available")
    public List<Mechanic> availableMechanics(
            @RequestParam(required = false) MechanicSpecialty specialty,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return mechanicService.getAvailableMechanics(specialty, start, end);
    }
}
