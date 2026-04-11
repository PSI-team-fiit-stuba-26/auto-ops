package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OperationRepository {
    private final ConcurrentHashMap<UUID, Operation> operations = new ConcurrentHashMap<>();

    public Operation save(Operation operation) {
        if (operation.id == null) {
            operation.id = UUID.randomUUID();
        }
        operations.put(operation.id, operation);
        return operation;
    }

    public Optional<Operation> findById(UUID id) {
        return Optional.ofNullable(operations.get(id));
    }

    public List<Operation> findAll() {
        return new ArrayList<>(operations.values());
    }
}
