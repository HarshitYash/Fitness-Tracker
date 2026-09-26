package com.project.fitness.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public NotificationService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:noreply@fitness-tracker.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendEmailOtp(String email, String code) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                log.info("Email OTP for {} is {}", email, code);
                printDevOtpBanner("Email Verification", email, code);
                return;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject("Your Fitness Tracker Verification Code");
                message.setText("Your OTP code is: " + code + ". It will expire in 5 minutes.");
                sender.send(message);
                log.info("Email OTP sent successfully to {}", email);
            } catch (Exception ex) {
                log.warn("Failed to send email via SMTP to {}: {}", email, ex.getMessage());
                System.err.println("Failed to send email via SMTP: " + ex.getMessage());
                printDevOtpBanner("Email Verification", email, code);
            }
        });
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        sendEmailOtp(toEmail, otpCode);
    }

    public void sendSmsOtp(String phoneNumber, String code) {
        log.info("SMS OTP for {} is {}", phoneNumber, code);
    }

    public void sendPasswordResetOtp(String email, String code) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                log.info("Password-reset OTP for {} is {}", email, code);
                printDevOtpBanner("Password Reset", email, code);
                return;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject("Your Fitness Tracker Password Reset Code");
                message.setText("Your password reset OTP is: " + code + ". It will expire in 5 minutes.");
                sender.send(message);
                log.info("Password-reset OTP sent successfully to {}", email);
            } catch (Exception ex) {
                log.warn("Failed to send reset email via SMTP to {}: {}", email, ex.getMessage());
                System.err.println("Failed to send reset email via SMTP: " + ex.getMessage());
                printDevOtpBanner("Password Reset", email, code);
            }
        });
    }

    private void printDevOtpBanner(String type, String destination, String code) {
        System.out.println("========== [DEVELOPMENT " + type.toUpperCase() + " OTP CODE] ==========");
        System.out.println("OTP for " + destination + " is: " + code);
        System.out.println("=================================================================");
    }
}
