package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Customer;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.RepairTask;
import sk.autoops.autoops.domain.Vehicle;
import sk.autoops.autoops.domain.enums.RepairFlag;
import sk.autoops.autoops.domain.enums.RepairOrderStatus;
import sk.autoops.autoops.domain.enums.RepairTaskStatus;
import sk.autoops.autoops.dto.CreateRepairOrderRequest;
import sk.autoops.autoops.infrastructure.RepairOrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RepairOrderService {
    private final RepairOrderRepository repairOrderRepository;
    private final CustomerService customerService;
    private final VehicleService vehicleService;

    public RepairOrderService(RepairOrderRepository repairOrderRepository, CustomerService customerService, VehicleService vehicleService) {
        this.repairOrderRepository = repairOrderRepository;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
    }

    public RepairOrder createRepairOrder(CreateRepairOrderRequest request) {
        Customer customer = resolveCustomer(request);
        Vehicle vehicle = resolveVehicle(request, customer.id);

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.status = RepairOrderStatus.SCHEDULED;
        repairOrder.problemDescription = request.problemDescription();
        repairOrder.maxPrice = request.maxPrice();
        repairOrder.receivedDate = LocalDateTime.now();
        repairOrder.plannedStart = request.plannedStart();
        repairOrder.plannedCompletionDate = request.plannedCompletionDate();
        repairOrder.customerId = customer.id;
        repairOrder.vehicleId = vehicle.id;
        repairOrder.mechanicId = request.mechanicId();
        List<String> taskNames = request.taskNames() == null || request.taskNames().isEmpty()
                ? List.of("Diagnose issue", "Repair vehicle", "Quality check")
                : request.taskNames();
        repairOrder.tasks = taskNames.stream()
                .map(name -> new RepairTask(UUID.randomUUID(), name, RepairTaskStatus.TODO))
                .toList();
        if (repairOrder.problemDescription.toLowerCase().contains("diagnostic")) {
            repairOrder.flags.add(RepairFlag.DIAGNOSTICS_REQUIRED);
        }

        RepairOrder saved = repairOrderRepository.save(repairOrder);
        vehicle.repairOrderIds.add(saved.id);
        vehicleService.updateVehicle(vehicle);
        return saved;
    }

    public RepairOrder getRepairOrder(UUID id) {
        return repairOrderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Repair order not found"));
    }

    public List<RepairOrder> findAll() {
        return repairOrderRepository.findAll();
    }

    private Customer resolveCustomer(CreateRepairOrderRequest request) {
        if (request.customerId() != null) {
            return customerService.findCustomer(request.customerId());
        }
        Customer existing = customerService.findByEmail(request.customerEmail());
        if (existing != null) {
            return existing;
        }
        if (request.customerName() == null || request.customerEmail() == null) {
            throw new IllegalArgumentException("Customer is not in system. Provide customerId or customer details.");
        }
        return customerService.registerCustomer(new Customer(null, request.customerName(), request.customerEmail(), request.customerPhone()));
    }

    private Vehicle resolveVehicle(CreateRepairOrderRequest request, UUID customerId) {
        if (request.vehicleId() != null) {
            return vehicleService.findVehicle(request.vehicleId());
        }
        Vehicle existing = vehicleService.findByVin(request.vin());
        if (existing != null) {
            return existing;
        }
        if (request.vin() == null || request.licensePlate() == null) {
            throw new IllegalArgumentException("Vehicle is not in system. Provide vehicleId or vehicle details.");
        }
        return vehicleService.registerVehicle(new Vehicle(null, request.vin(), request.licensePlate(), request.brand(), request.model(), request.year(), request.mileage(), customerId));
    }
}
