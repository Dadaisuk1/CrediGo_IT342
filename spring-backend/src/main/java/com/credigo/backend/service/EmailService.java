package com.credigo.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Convenience methods for common email types
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        sendEmail(to, "Welcome to CrediGo", "welcome-email", context);
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        sendEmail(to, "Password Reset Request", "password-reset-email", context);
    }

    public void sendTransactionConfirmationEmail(String to, String username, String transactionDetails) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("transactionDetails", transactionDetails);
        sendEmail(to, "Transaction Confirmation", "transaction-confirmation-email", context);
    }
}
