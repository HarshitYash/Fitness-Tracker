package com.project.fitness.service;

import com.project.fitness.entity.OtpChannel;
import com.project.fitness.entity.OtpCode;
import com.project.fitness.exception.BadRequestException;
import com.project.fitness.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "ttlMinutes", 5);
        ReflectionTestUtils.setField(otpService, "exposeInResponse", true);
    }

    @Test
    void testCreateAndSendEmailOtp() {
        String email = "test@example.com";
        String code = otpService.createAndSend(email, OtpChannel.EMAIL);

        assertNotNull(code);
        assertEquals(6, code.length());
        verify(otpCodeRepository).deleteByDestinationAndChannel(email, OtpChannel.EMAIL);
        verify(otpCodeRepository).save(any(OtpCode.class));
        verify(notificationService).sendEmailOtp(email, code);
    }

    @Test
    void testVerifyValidOtp() {
        String destination = "test@example.com";
        String code = "123456";
        OtpCode otpCode = OtpCode.builder()
                .destination(destination)
                .channel(OtpChannel.EMAIL)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpCodeRepository.findTopByDestinationAndChannelAndUsedFalseOrderByExpiresAtDesc(destination, OtpChannel.EMAIL))
                .thenReturn(Optional.of(otpCode));

        otpService.verify(destination, OtpChannel.EMAIL, code);
        assertTrue(otpCode.isUsed());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void testVerifyExpiredOtpThrowsException() {
        String destination = "test@example.com";
        String code = "123456";
        OtpCode otpCode = OtpCode.builder()
                .destination(destination)
                .channel(OtpChannel.EMAIL)
                .code(code)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(otpCodeRepository.findTopByDestinationAndChannelAndUsedFalseOrderByExpiresAtDesc(destination, OtpChannel.EMAIL))
                .thenReturn(Optional.of(otpCode));

        assertThrows(BadRequestException.class, () -> otpService.verify(destination, OtpChannel.EMAIL, code));
    }

    @Test
    void testVerifyWrongCodeThrowsException() {
        String destination = "test@example.com";
        OtpCode otpCode = OtpCode.builder()
                .destination(destination)
                .channel(OtpChannel.EMAIL)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpCodeRepository.findTopByDestinationAndChannelAndUsedFalseOrderByExpiresAtDesc(destination, OtpChannel.EMAIL))
                .thenReturn(Optional.of(otpCode));

        assertThrows(BadRequestException.class, () -> otpService.verify(destination, OtpChannel.EMAIL, "999999"));
    }
}
