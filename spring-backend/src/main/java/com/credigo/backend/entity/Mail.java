package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a message sent to a user after a transaction
 */
@Entity
@Table(name = "mails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mailid")
    private Long mailId;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "body", length = 255)
    private String body;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "transactionid")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
} 