package com.credigo.backend.controller;

import com.credigo.backend.entity.Mail;
import com.credigo.backend.service.MailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailController {

    private static final Logger log = LoggerFactory.getLogger(MailController.class);
    
    private final MailService mailService;
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserMails() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
            
            List<Mail> mails = mailService.getCurrentUserMails();
            return ResponseEntity.ok(mails);
        } catch (Exception e) {
            log.error("Error getting current user mails: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving mails: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMails(@PathVariable Long userId) {
        try {
            List<Mail> mails = mailService.getUserMails(userId);
            return ResponseEntity.ok(mails);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting mails for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving mails: " + e.getMessage());
        }
    }
    
    @GetMapping("/{mailId}")
    public ResponseEntity<?> getMailById(@PathVariable Long mailId) {
        try {
            Mail mail = mailService.getMailById(mailId);
            return ResponseEntity.ok(mail);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting mail {}: {}", mailId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving mail: " + e.getMessage());
        }
    }
    
    @PostMapping("/createMail")
    public ResponseEntity<?> createMail(@RequestBody Mail mail) {
        try {
            Mail createdMail = mailService.createMail(mail);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMail);
        } catch (Exception e) {
            log.error("Error creating mail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating mail: " + e.getMessage());
        }
    }
    
    @PostMapping("/transaction")
    public ResponseEntity<?> createTransactionMail(
            @RequestParam Long userId,
            @RequestParam Long transactionId,
            @RequestParam String subject,
            @RequestParam String body) {
        try {
            Mail createdMail = mailService.createTransactionMail(userId, transactionId, subject, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMail);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating transaction mail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating transaction mail: " + e.getMessage());
        }
    }
    
    @PutMapping("/{mailId}/read")
    public ResponseEntity<?> markMailAsRead(@PathVariable Long mailId) {
        try {
            Mail updatedMail = mailService.markMailAsRead(mailId);
            return ResponseEntity.ok(updatedMail);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error marking mail {} as read: {}", mailId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating mail: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{mailId}")
    public ResponseEntity<?> deleteMail(@PathVariable Long mailId) {
        try {
            mailService.deleteMail(mailId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mail deleted successfully");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting mail {}: {}", mailId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting mail: " + e.getMessage());
        }
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getMailsByTransactionId(@PathVariable Long transactionId) {
        try {
            List<Mail> mails = mailService.getMailsByTransactionId(transactionId);
            return ResponseEntity.ok(mails);
        } catch (Exception e) {
            log.error("Error getting mails for transaction {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving mails: " + e.getMessage());
        }
    }
} 