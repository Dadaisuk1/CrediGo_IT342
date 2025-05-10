package com.credigo.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.credigo.backend.service.EmailService;
import com.credigo.backend.service.NotificationService;
import com.credigo.backend.service.NotificationService.NotificationType;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Test endpoint for sending a welcome email
     * @param email The recipient email address
     * @return Success message
     */
    @GetMapping("/email/welcome/{email}")
    public ResponseEntity<String> testWelcomeEmail(@PathVariable String email) {
        emailService.sendWelcomeEmail(email, "TestUser");
        return ResponseEntity.ok("Welcome email test sent to " + email);
    }

    /**
     * Test endpoint for sending a notification to a user
     * @param userId The user ID to send the notification to
     * @return Success message
     */
    @GetMapping("/notification/{userId}")
    public ResponseEntity<String> testNotification(@PathVariable String userId) {
        notificationService.sendInfoNotification(userId, "This is a test notification from the API");
        return ResponseEntity.ok("Notification sent to user ID: " + userId);
    }

    /**
     * Test endpoint for sending a global notification
     * @return Success message
     */
    @GetMapping("/notification/global")
    public ResponseEntity<String> testGlobalNotification() {
        notificationService.sendGlobalInfoNotification("This is a global test notification");
        return ResponseEntity.ok("Global notification sent");
    }
}
