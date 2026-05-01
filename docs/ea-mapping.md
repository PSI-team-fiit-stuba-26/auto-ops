# EA Model Mapping

Source model inspected:

- `/Users/yuriioliinyk/fiitstu/2rocnik/ls/psi/project/4/PSI AutoOps Submission 4.qea`

The QEA file is a SQLite Enterprise Architect project. The implementation follows the package diagram named `PD_ArchitekturalneVrstvy`:

| EA package | Code package | Purpose |
| --- | --- | --- |
| `presentation` | `sk.autoops.autoops.presentation` | REST controllers for web/API workflows |
| `dto` | `sk.autoops.autoops.dto` | Request/response records for use-case boundaries |
| `application` | `sk.autoops.autoops.application` | Use-case services and orchestration |
| `domain` | `sk.autoops.autoops.domain` | Core business entities |
| `enums` | `sk.autoops.autoops.domain.enums` | State and role enumerations |
| `infrastructure` | `sk.autoops.autoops.infrastructure` | In-memory repositories |
| `external` | `sk.autoops.autoops.external` | Billing/payment integration facades |

## Use Cases

| EA use case | Backend entry point | Main service |
| --- | --- | --- |
| `UC01 Naplanuj a prirad opravu` | `ScheduleRepairController` | `SchedulingService`, `RepairOrderService` |
| `UC02 Pouzi diel zo skladu` | `UseReserveInventoryItemController` | `UseReserveInventoryItemService`, `InventoryService` |
| `UC03 Aktualizuj ceny a sluzby` | `OperationController` | `OperationService` |
| `UC04 Dokonci opravu a vystav fakturu` | `RepairCompletionController` | `RepairCompletionService` |

## Class Diagram Continuity

Classes from the EA overall class diagram are represented in code:

- Controllers: `PlanningController`, `ScheduleRepairController`, `UseReserveInventoryItemController`, `OperationController`, `RepairCompletionController`.
- DTOs: `CreateRepairOrderRequest`, `ScheduleRepairResponse`, `UseInventoryItemRequest`, `ReserveInventoryItemRequest`, `CreateOperationRequest`, `UpdateOperationRequest`, `CompleteRepairRequest`, `CompleteRepairResponse`, `InvoiceSummary`.
- Application services: `CustomerService`, `VehicleService`, `MechanicService`, `SchedulingService`, `RepairOrderService`, `InventoryService`, `UseReserveInventoryItemService`, `OperationService`, `RepairCompletionService`, `NotificationService`, `UserService`.
- Domain: `Customer`, `Vehicle`, `Mechanic`, `RepairOrder`, `RepairTask`, `UsedPart`, `InventoryItem`, `Reservation`, `UsageReservationRecord`, `Operation`, `PriceChangeLog`, `Notification`, `ServiceHistoryEntry`, `User`.
- Enums: `InventoryStatus`, `MechanicSpecialty`, `NotificationType`, `PaymentStatus`, `RepairFlag`, `RepairOrderStatus`, `RepairTaskStatus`, `UserRole`.
- Infrastructure: repositories for customer, vehicle, mechanic, repair order, inventory item, reservation, usage, operation, notification, service history and user storage.
- External interfaces from EA are represented as `BillingGateway` and `PaymentGateway`.

## Intentional Deviations

- Repositories are in-memory Spring beans instead of JPA repositories. This keeps the project runnable without database setup while retaining the modeled repository layer.
- `PriceChangeLogRepository` is added even though the extracted package listing did not expose it as a top-level EA repository. The price-update use case needs persistent audit records.
- Some EA duplicate class entries were consolidated into one canonical class in the layered package structure.
