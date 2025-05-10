package com.credigo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public void sendNotification(String userId, String message, NotificationType type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type.name());
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSendToUser(
            userId,
            "/topic/notifications",
            notification
        );
    }

    public void sendGlobalNotification(String message, NotificationType type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type.name());
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/global", notification);
    }

    // Convenience methods for different notification types
    public void sendInfoNotification(String userId, String message) {
        sendNotification(userId, message, NotificationType.INFO);
    }

    public void sendSuccessNotification(String userId, String message) {
        sendNotification(userId, message, NotificationType.SUCCESS);
    }

    public void sendWarningNotification(String userId, String message) {
        sendNotification(userId, message, NotificationType.WARNING);
    }

    public void sendErrorNotification(String userId, String message) {
        sendNotification(userId, message, NotificationType.ERROR);
    }

    // Global convenience methods
    public void sendGlobalInfoNotification(String message) {
        sendGlobalNotification(message, NotificationType.INFO);
    }

    public void sendGlobalSuccessNotification(String message) {
        sendGlobalNotification(message, NotificationType.SUCCESS);
    }

    public void sendGlobalWarningNotification(String message) {
        sendGlobalNotification(message, NotificationType.WARNING);
    }

    public void sendGlobalErrorNotification(String message) {
        sendGlobalNotification(message, NotificationType.ERROR);
    }
}
