package com.credigo.backend.service;

import com.credigo.backend.entity.Mail;
import java.util.List;

public interface MailService {
    
    /**
     * Create a new mail
     * @param mail the mail entity to create
     * @return the created mail
     */
    Mail createMail(Mail mail);
    
    /**
     * Create a transaction-related mail for a user
     * @param userId the ID of the user
     * @param transactionId the ID of the transaction
     * @param subject the mail subject
     * @param body the mail body content
     * @return the created mail
     */
    Mail createTransactionMail(Long userId, Long transactionId, String subject, String body);
    
    /**
     * Get all mails for the current authenticated user
     * @return list of mails
     */
    List<Mail> getCurrentUserMails();
    
    /**
     * Get all mails for a specific user
     * @param userId the user ID
     * @return list of mails
     */
    List<Mail> getUserMails(Long userId);
    
    /**
     * Get a mail by its ID
     * @param mailId the mail ID
     * @return the mail if found
     */
    Mail getMailById(Long mailId);
    
    /**
     * Mark a mail as read
     * @param mailId the mail ID
     * @return the updated mail
     */
    Mail markMailAsRead(Long mailId);
    
    /**
     * Delete a mail
     * @param mailId the mail ID
     */
    void deleteMail(Long mailId);
    
    /**
     * Get all mails related to a transaction
     * @param transactionId the transaction ID
     * @return list of transaction-related mails
     */
    List<Mail> getMailsByTransactionId(Long transactionId);
} 