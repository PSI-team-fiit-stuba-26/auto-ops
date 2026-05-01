package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    public UUID id;
    public String name;
    public String email;
    public UserRole role;
    public LocalDateTime createdAt;

    public User() {
    }
}
