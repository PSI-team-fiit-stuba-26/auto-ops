package sk.autoops.autoops.domain;

import sk.autoops.autoops.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
    public UUID id;
    public String message;
    public NotificationType type;
    public UUID recipientId;
    public LocalDateTime sentAt;

    public Notification() {
    }
}
