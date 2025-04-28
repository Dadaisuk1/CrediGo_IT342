package com.credigo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class EmailNotificationService {

  @Autowired
  private JavaMailSender mailSender;

  public void sendTopUpSuccessEmail(String userEmail, BigDecimal amount) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(userEmail);
    message.setSubject("Top-Up Successful");
    message.setText("Your wallet has been successfully topped up with " + amount + ". Thank you for using CrediGo!");
    mailSender.send(message);
  }
}
