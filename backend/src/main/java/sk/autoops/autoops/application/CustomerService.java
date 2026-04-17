package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Customer;
import sk.autoops.autoops.infrastructure.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    public Customer registerCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(UUID id, String name, String email, String phone) {
        Customer customer = findCustomer(id);
        if (name != null && !name.isBlank()) customer.name = name;
        if (email != null && !email.isBlank()) customer.email = email;
        if (phone != null && !phone.isBlank()) customer.phone = phone;
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
