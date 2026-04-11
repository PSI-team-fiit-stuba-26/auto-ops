package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.User;
import sk.autoops.autoops.domain.enums.UserRole;
import sk.autoops.autoops.infrastructure.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email) {
        return userRepository.findAll().stream()
                .filter(user -> user.email.equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public User register(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.getUsersByRole(role);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
