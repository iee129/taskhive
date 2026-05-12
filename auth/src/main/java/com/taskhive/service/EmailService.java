package com.taskhive.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${MAIL_FROM:noreply@taskhive.app}")
    private String fromAddress;

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("token", token);
        ctx.setVariable("verifyUrl", "https://taskhive.vercel.app/verify-email?token=" + token);
        sendHtml(to, "TaskHive 이메일 인증", "email/verification", ctx);
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("token", token);
        ctx.setVariable("resetUrl", "https://taskhive.vercel.app/reset-password?token=" + token);
        sendHtml(to, "TaskHive 비밀번호 재설정", "email/reset-password", ctx);
    }

    private void sendHtml(String to, String subject, String template, Context ctx) {
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
