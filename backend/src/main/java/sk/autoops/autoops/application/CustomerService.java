package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Customer;
import sk.autoops.autoops.infrastructure.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ContactValidator contactValidator;

    public CustomerService(CustomerRepository customerRepository, ContactValidator contactValidator) {
        this.customerRepository = customerRepository;
        this.contactValidator = contactValidator;
    }

    public Customer findCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public Customer findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return customerRepository.findByEmail(contactValidator.normalizeEmail(email)).orElse(null);
    }

    public Customer registerCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        contactValidator.requireValidName(customer.name);
        contactValidator.requireValidEmail(customer.email);
        contactValidator.requireValidPhone(customer.phone);
        customer.email = contactValidator.normalizeEmail(customer.email);
        if (customerRepository.findByEmail(customer.email).isPresent()) {
            throw new IllegalStateException("Customer with this email already exists");
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(UUID id, String name, String email, String phone) {
        Customer customer = findCustomer(id);
        if (name != null && !name.isBlank()) {
            contactValidator.requireValidName(name);
            customer.name = name.trim();
        }
        if (email != null && !email.isBlank()) {
            contactValidator.requireValidEmail(email);
            String normalized = contactValidator.normalizeEmail(email);
            customerRepository.findByEmail(normalized).ifPresent(existing -> {
                if (!existing.id.equals(customer.id)) {
                    throw new IllegalStateException("Customer with this email already exists");
                }
            });
            customer.email = normalized;
        }
        if (phone != null && !phone.isBlank()) {
            contactValidator.requireValidPhone(phone);
            customer.phone = phone;
        }
        return customerRepository.save(customer);
    }

    public void deleteCustomer(UUID id) {
        if (!customerRepository.delete(id)) {
            throw new IllegalArgumentException("Customer not found");
        }
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
