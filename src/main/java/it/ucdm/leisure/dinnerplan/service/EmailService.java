package it.ucdm.leisure.dinnerplan.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @org.springframework.scheduling.annotation.Async
    public void sendSimpleMessage(String to, String subject, String text) {
        if (to == null || to.trim().isEmpty()) {
            logger.warn("Skipping email sending: recipient email is null or empty. Subject: {}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@dinnerplan.com"); // Configure this property or use a default
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent to: {}", to);
        } catch (Throwable t) {
            logger.error("Best-effort email sending failed to: {}. Error: {}", to, t.getMessage());
        }
    }
}
