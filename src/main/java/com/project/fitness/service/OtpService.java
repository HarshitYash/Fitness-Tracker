package com.project.fitness.service;

import com.project.fitness.entity.OtpChannel;
import com.project.fitness.entity.OtpCode;
import com.project.fitness.exception.BadRequestException;
import com.project.fitness.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpCodeRepository otpCodeRepository;
    private final NotificationService notificationService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.ttl-minutes:5}")
    private int ttlMinutes;

    @Value("${app.otp.expose-in-response:true}")
    private boolean exposeInResponse;

    @Transactional
    public String createAndSend(String destination, OtpChannel channel) {
        otpCodeRepository.deleteByDestinationAndChannel(destination, channel);
        String code = String.format("%06d", random.nextInt(1_000_000));
        otpCodeRepository.save(OtpCode.builder()
                .destination(destination)
                .channel(channel)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .used(false)
                .build());

        if (channel == OtpChannel.SMS) {
            notificationService.sendSmsOtp(destination, code);
        } else if (channel == OtpChannel.PASSWORD_RESET) {
            notificationService.sendPasswordResetOtp(destination, code);
        } else {
            notificationService.sendEmailOtp(destination, code);
        }
        return exposeInResponse ? code : null;
    }

    @Transactional
    public void verify(String destination, OtpChannel channel, String code) {
        OtpCode otp = otpCodeRepository
                .findTopByDestinationAndChannelAndUsedFalseOrderByExpiresAtDesc(destination, channel)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request a new one."));
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        if (!otp.getCode().equals(code.trim())) {
            throw new BadRequestException("Invalid OTP");
        }
        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    public boolean isExposeEnabled() {
        return exposeInResponse;
    }
}
