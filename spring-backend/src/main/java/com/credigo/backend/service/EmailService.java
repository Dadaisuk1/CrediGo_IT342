package com.credigo.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.from:noreply@credigo.com}")
    private String fromEmail;

    /**
     * Send welcome email to new users
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        if (!isMailEnabled()) {
            log.info("Email sending disabled. Would send welcome email to: {}", to);
            return;
        }

        Context context = new Context();
        context.setVariable("username", username);

        String subject = "Welcome to CrediGo!";
        String htmlContent = templateEngine.process("welcome-email", context);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        if (!isMailEnabled()) {
            log.info("Email sending disabled. Would send password reset email to: {}", to);
            return;
        }

        Context context = new Context();
        context.setVariable("resetLink", resetLink);

        String subject = "Reset Your CrediGo Password";
        String htmlContent = templateEngine.process("password-reset-email", context);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Send transaction confirmation email
     */
    @Async
    public void sendTransactionConfirmationEmail(String to, String username, String transactionDetails) {
        if (!isMailEnabled()) {
            log.info("Email sending disabled. Would send transaction confirmation to: {}", to);
            return;
        }

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("transactionDetails", transactionDetails);

        String subject = "CrediGo Transaction Confirmation";
        String htmlContent = templateEngine.process("transaction-confirmation-email", context);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Generic method to send email with HTML content
     */
    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        if (!isMailEnabled()) {
            log.info("Email sending disabled. Would send email to: {} with subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    /**
     * Send email with template and dynamic variables
     */
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!isMailEnabled()) {
            log.info("Email sending disabled. Would send templated email to: {}", to);
            return;
        }

        Context context = new Context();
        variables.forEach(context::setVariable);

        String htmlContent = templateEngine.process(templateName, context);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Check if email sending is enabled
     */
    private boolean isMailEnabled() {
        return mailEnabled && mailSender != null;
    }
}
