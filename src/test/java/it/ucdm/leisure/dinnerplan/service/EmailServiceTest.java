package it.ucdm.leisure.dinnerplan.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EmailServiceTest {

    private JavaMailSender emailSender;
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        emailSender = Mockito.mock(JavaMailSender.class);
        emailService = new EmailService(emailSender);
    }

    @Test
    public void testSendSimpleMessage_ValidEmail() {
        emailService.sendSimpleMessage("test@example.com", "Subject", "Text");
        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendSimpleMessage_NullEmail() {
        emailService.sendSimpleMessage(null, "Subject", "Text");
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendSimpleMessage_EmptyEmail() {
        emailService.sendSimpleMessage("", "Subject", "Text");
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendSimpleMessage_BlankEmail() {
        emailService.sendSimpleMessage("   ", "Subject", "Text");
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }
}
