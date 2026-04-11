package sk.autoops.autoops.domain;

import java.util.UUID;

public class Customer {
    public UUID id;
    public String name;
    public String email;
    public String phone;

    public Customer() {
    }

    public Customer(UUID id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
