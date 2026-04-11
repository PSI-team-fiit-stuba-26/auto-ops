package sk.autoops.autoops.presentation;

import org.springframework.web.bind.annotation.*;
import sk.autoops.autoops.application.CustomerService;
import sk.autoops.autoops.application.MechanicService;
import sk.autoops.autoops.application.VehicleService;
import sk.autoops.autoops.domain.Customer;
import sk.autoops.autoops.domain.Mechanic;
import sk.autoops.autoops.domain.Vehicle;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;

import java.time.LocalDateTime;
import java.util.List;

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

    @PostMapping("/customers")
    public Customer newCustomer(@RequestBody Customer customer) {
        return customerService.registerCustomer(customer);
    }

    @GetMapping("/vehicles")
    public List<Vehicle> vehicles() {
        return vehicleService.findAll();
    }

    @GetMapping("/vehicles/search")
    public Vehicle checkVehicle(@RequestParam String vin) {
        return vehicleService.findByVin(vin);
    }

    @PostMapping("/vehicles")
    public Vehicle newVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.registerVehicle(vehicle);
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
