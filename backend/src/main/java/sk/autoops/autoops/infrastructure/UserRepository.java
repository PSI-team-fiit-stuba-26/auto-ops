package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.User;
import sk.autoops.autoops.domain.enums.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.id == null) {
            user.id = UUID.randomUUID();
        }
        users.put(user.id, user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> getUsersByRole(UserRole role) {
        return users.values().stream()
                .filter(user -> user.role == role)
                .toList();
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
