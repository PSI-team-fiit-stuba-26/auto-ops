package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NotificationRepository {
    private final ConcurrentHashMap<UUID, Notification> notifications = new ConcurrentHashMap<>();

    public Notification save(Notification notification) {
        if (notification.id == null) {
            notification.id = UUID.randomUUID();
        }
        notifications.put(notification.id, notification);
        return notification;
    }

    public List<Notification> findAll() {
        return new ArrayList<>(notifications.values());
    }
}
