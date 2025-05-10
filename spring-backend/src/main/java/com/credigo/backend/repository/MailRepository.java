package com.credigo.backend.repository;

import com.credigo.backend.entity.Mail;
import com.credigo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {
    
    /**
     * Find all mails belonging to a specific user
     * @param user the user entity
     * @return list of mails for the user
     */
    List<Mail> findByUser(User user);
    
    /**
     * Find all mails related to a specific transaction
     * @param transactionId the transaction ID
     * @return list of mails for the transaction
     */
    List<Mail> findByTransactionId(Long transactionId);
    
    /**
     * Find all unread mails for a user
     * @param user the user entity
     * @return list of unread mails
     */
    List<Mail> findByUserAndIsReadFalse(User user);
} 