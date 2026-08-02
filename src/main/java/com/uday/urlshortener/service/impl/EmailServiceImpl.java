package com.uday.urlshortener.service.impl;

import com.uday.urlshortener.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;
        String subject = "Verify your Shortify Account";
        String text = "Welcome to Shortify!\n\nPlease click the link below to verify your email address:\n"
                + verificationUrl + "\n\nIf you did not create an account, please ignore this email.";

        sendEmailSafely(toEmail, subject, text, "Verification Link: " + verificationUrl);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String subject = "Password Reset Request - Shortify";
        String text = "Hello,\n\nYou requested a password reset for your Shortify account.\n"
                + "Please click the link below to set a new password:\n"
                + resetUrl + "\n\nThis token will expire in 30 minutes.\n"
                + "If you did not request a password reset, please ignore this email.";

        sendEmailSafely(toEmail, subject, text, "Password Reset Link: " + resetUrl);
    }

    private void sendEmailSafely(String to, String subject, String content, String logFallbackMessage) {
        try {
            if (mailFrom == null || mailFrom.isBlank()) {
                log.info("[SMTP Not Configured] To: {} | Subject: {} | {}", to, subject, logFallbackMessage);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email successfully sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email to {} via SMTP (Fallback Logged): {}", to, e.getMessage());
            log.info("[EMAIL FALLBACK] To: {} | {}", to, logFallbackMessage);
        }
    }
}
