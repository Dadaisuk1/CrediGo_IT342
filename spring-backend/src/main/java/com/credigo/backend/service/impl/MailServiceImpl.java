package com.credigo.backend.service.impl;

import com.credigo.backend.entity.Mail;
import com.credigo.backend.entity.User;
import com.credigo.backend.repository.MailRepository;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.service.MailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    
    private final MailRepository mailRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public Mail createMail(Mail mail) {
        log.info("Creating new mail with subject: {}", mail.getSubject());
        return mailRepository.save(mail);
    }
    
    @Override
    @Transactional
    public Mail createTransactionMail(Long userId, Long transactionId, String subject, String body) {
        log.info("Creating transaction mail for user: {} and transaction: {}", userId, transactionId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Mail mail = new Mail();
        mail.setUser(user);
        mail.setTransactionId(transactionId);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setIsRead(false);
        
        return mailRepository.save(mail);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Mail> getCurrentUserMails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Optional<User> currentUser = userRepository.findByUsername(username);
        if (currentUser.isEmpty()) {
            log.warn("Attempted to get mails for non-existent user: {}", username);
            throw new EntityNotFoundException("User not found: " + username);
        }
        
        return mailRepository.findByUser(currentUser.get());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Mail> getUserMails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return mailRepository.findByUser(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Mail getMailById(Long mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found with id: " + mailId));
    }
    
    @Override
    @Transactional
    public Mail markMailAsRead(Long mailId) {
        Mail mail = getMailById(mailId);
        mail.setIsRead(true);
        return mailRepository.save(mail);
    }
    
    @Override
    @Transactional
    public void deleteMail(Long mailId) {
        if (!mailRepository.existsById(mailId)) {
            throw new EntityNotFoundException("Mail not found with id: " + mailId);
        }
        
        mailRepository.deleteById(mailId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Mail> getMailsByTransactionId(Long transactionId) {
        return mailRepository.findByTransactionId(transactionId);
    }
} 