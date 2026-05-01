package sk.autoops.autoops.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sk.autoops.autoops.domain.*;
import sk.autoops.autoops.domain.enums.MechanicSpecialty;
import sk.autoops.autoops.domain.enums.RepairOrderStatus;
import sk.autoops.autoops.domain.enums.RepairTaskStatus;
import sk.autoops.autoops.domain.enums.UserRole;
import sk.autoops.autoops.infrastructure.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {
    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID MECHANIC_ENGINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    public static final UUID MECHANIC_ELECTRIC_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    public static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    public static final UUID VEHICLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    public static final UUID REPAIR_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");
    public static final UUID BRAKE_PAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    public static final UUID OIL_FILTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    public static final UUID DIAGNOSTICS_OPERATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OperationRepository operationRepository;

    public DataSeeder(
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            RepairOrderRepository repairOrderRepository,
            InventoryItemRepository inventoryItemRepository,
            OperationRepository operationRepository
    ) {
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedMechanics();
        seedCustomerAndVehicle();
        seedInventory();
        seedOperations();
        seedRepairOrder();
    }

    private void seedUsers() {
        User admin = new User();
        admin.id = ADMIN_ID;
        admin.name = "Admin Manager";
        admin.email = "admin@autoops.local";
        admin.role = UserRole.ADMIN;
        admin.createdAt = LocalDateTime.now();
        userRepository.save(admin);
    }

    private void seedMechanics() {
        mechanicRepository.save(new Mechanic(
                MECHANIC_ENGINE_ID,
                "Marek Engine",
                List.of(MechanicSpecialty.GENERAL, MechanicSpecialty.ENGINE, MechanicSpecialty.DIAGNOSTICS),
                "No EV high-voltage work",
                new BigDecimal("38.50")
        ));
        mechanicRepository.save(new Mechanic(
                MECHANIC_ELECTRIC_ID,
                "Eva Electrics",
                List.of(MechanicSpecialty.ELECTRICAL, MechanicSpecialty.DIAGNOSTICS),
                "Electrical jobs only",
                new BigDecimal("42.00")
        ));
    }

    private void seedCustomerAndVehicle() {
        customerRepository.save(new Customer(CUSTOMER_ID, "Peter Novak", "peter.novak@example.com", "+421900111222"));
        vehicleRepository.save(new Vehicle(VEHICLE_ID, "WVWZZZ1KZ6W000001", "BA-123AB", "Volkswagen", "Golf", 2018, 162000, CUSTOMER_ID));
    }

    private void seedInventory() {
        inventoryItemRepository.save(new InventoryItem(BRAKE_PAD_ID, "BRK-PAD-ATE", "ATE brake pads", 8, "A1", new BigDecimal("64.90")));
        inventoryItemRepository.save(new InventoryItem(OIL_FILTER_ID, "FIL-OIL-MANN", "MANN oil filter", 5, "B4", new BigDecimal("11.50")));
    }

    private void seedOperations() {
        Operation diagnostics = new Operation();
        diagnostics.id = DIAGNOSTICS_OPERATION_ID;
        diagnostics.name = "Computer diagnostics";
        diagnostics.description = "Read error codes, inspect live data and produce an initial estimate.";
        diagnostics.currentPrice = new BigDecimal("49.00");
        diagnostics.createdAt = LocalDateTime.now();
        diagnostics.updatedAt = diagnostics.createdAt;
        diagnostics.eligibleWorkers = List.of(MECHANIC_ENGINE_ID, MECHANIC_ELECTRIC_ID);
        operationRepository.save(diagnostics);
    }

    private void seedRepairOrder() {
        RepairOrder order = new RepairOrder();
        order.id = REPAIR_ORDER_ID;
        order.status = RepairOrderStatus.SCHEDULED;
        order.problemDescription = "Brake noise and dashboard warning light";
        order.maxPrice = new BigDecimal("400.00");
        order.receivedDate = LocalDateTime.now().minusDays(1);
        order.plannedStart = LocalDateTime.now().plusHours(2);
        order.plannedCompletionDate = LocalDateTime.now().plusHours(5);
        order.customerId = CUSTOMER_ID;
        order.vehicleId = VEHICLE_ID;
        order.mechanicId = MECHANIC_ENGINE_ID;
        order.tasks = List.of(
                new RepairTask(UUID.randomUUID(), "Diagnose warning light", RepairTaskStatus.TODO),
                new RepairTask(UUID.randomUUID(), "Inspect brake system", RepairTaskStatus.TODO)
        );
        repairOrderRepository.save(order);
    }
}
