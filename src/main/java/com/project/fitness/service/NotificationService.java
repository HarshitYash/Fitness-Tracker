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
                return;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject("Fitness Tracker verification code");
                message.setText("Your email verification OTP is " + code + ". It expires in a few minutes.");
                sender.send(message);
                log.info("Email OTP sent to {}", email);
            } catch (Exception ex) {
                log.warn("Could not send email to {}. Use the OTP shown in the app (dev mode) or check mail settings. OTP={}",
                        email, code);
            }
        });
    }

    public void sendSmsOtp(String phoneNumber, String code) {
        log.info("SMS OTP for {} is {}", phoneNumber, code);
    }

    public void sendPasswordResetOtp(String email, String code) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                log.info("Password-reset OTP for {} is {}", email, code);
                return;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject("Fitness Tracker password reset code");
                message.setText("Your password reset OTP is " + code + ". It expires in a few minutes.");
                sender.send(message);
                log.info("Password-reset OTP sent to {}", email);
            } catch (Exception ex) {
                log.warn("Could not send reset email to {}. OTP={}", email, code);
            }
        });
    }
}
