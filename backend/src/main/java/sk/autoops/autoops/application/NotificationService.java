package sk.autoops.autoops.application;

import org.springframework.stereotype.Service;
import sk.autoops.autoops.domain.Notification;
import sk.autoops.autoops.domain.Operation;
import sk.autoops.autoops.domain.PriceChangeLog;
import sk.autoops.autoops.domain.RepairOrder;
import sk.autoops.autoops.domain.enums.NotificationType;
import sk.autoops.autoops.infrastructure.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyRepairCompleted(RepairOrder repairOrder) {
        sendNotification("Repair " + repairOrder.id + " was completed and invoice was created.", NotificationType.REPAIR_COMPLETED, repairOrder.customerId);
    }

    public void notifyMechanicAssigned(RepairOrder repairOrder, String mechanicName) {
        String message = "New order assigned to " + mechanicName + ": \"" + repairOrder.problemDescription + "\" — scheduled for " + repairOrder.plannedStart + ".";
        sendNotification(message, NotificationType.ORDER_ASSIGNED, repairOrder.mechanicId);
    }

    public void sendPriceChangeNotification(Operation operation, PriceChangeLog log) {
        sendNotification("Operation " + operation.name + " price changed from " + log.oldPrice + " to " + log.newPrice + ".", NotificationType.PRICE_CHANGE, log.changedBy);
    }

    public Notification sendNotification(String message, NotificationType type, UUID recipientId) {
        Notification notification = new Notification();
        notification.message = message;
        notification.type = type;
        notification.recipientId = recipientId;
        notification.sentAt = LocalDateTime.now();
        return saveNotification(notification);
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
}
