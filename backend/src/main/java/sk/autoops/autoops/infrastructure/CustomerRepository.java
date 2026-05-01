package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepository {
    private final ConcurrentHashMap<UUID, Customer> customers = new ConcurrentHashMap<>();

    public Customer save(Customer customer) {
        if (customer.id == null) {
            customer.id = UUID.randomUUID();
        }
        customers.put(customer.id, customer);
        return customer;
    }

    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(customers.get(id));
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return customers.values().stream()
                .filter(customer -> email.equalsIgnoreCase(customer.email))
                .findFirst();
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }
}
