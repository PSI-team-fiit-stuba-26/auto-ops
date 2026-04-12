# PSI AutoOps Manager

Full-stack school project implementation for the first four modeled use cases.

## Implemented Use Cases

- UC01 Schedule and assign repair: create customer/vehicle when missing, check mechanic availability, create repair order and tasks.
- UC02 Use or reserve inventory item: search stock, reserve parts, consume parts, record usage against repair orders.
- UC03 Update prices and services: list operations, update price/details, create price-change audit log and notification.
- UC04 Complete repair and issue invoice: validate tasks, record work hours, create service history, calculate invoice and payment request.

## Run

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm start
```

Open `http://localhost:5173`. The frontend expects the backend at `http://localhost:8080`.

## Project Layout

- `backend/`: Spring Boot 4 project, Java 21, Maven wrapper.
- `frontend/`: Dependency-free web UI served by Node's built-in HTTP server.
- `docs/ea-mapping.md`: Mapping from the supplied Enterprise Architect model to implementation packages/classes.

## Seed Data

The backend starts with sample mechanics, customer, vehicle, repair order, inventory items and one service operation. Useful fixed IDs are declared in `backend/src/main/java/sk/autoops/autoops/config/DataSeeder.java`.

## Verification Notes

Frontend syntax check:

```bash
cd frontend
npm run check
```

The Maven wrapper test run could not be completed in this sandbox because Maven needed to write/download outside the allowed filesystem area and the escalation request was rejected by the environment. Run `cd backend && ./mvnw test` locally after dependencies download.
