package com.taskhive.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;
    @InjectMocks EmailService emailService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@taskhive.app");
    }

    @Test
    void sendVerificationEmail_callsMailSender() {
        MimeMessage msg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>test</html>");

        emailService.sendVerificationEmail("test@example.com", "홍길동", "token123");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_callsMailSender() {
        MimeMessage msg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>test</html>");

        emailService.sendPasswordResetEmail("test@example.com", "홍길동", "reset123");

        verify(mailSender).send(any(MimeMessage.class));
    }
}
