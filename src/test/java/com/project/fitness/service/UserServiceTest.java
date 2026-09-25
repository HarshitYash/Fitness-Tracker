package com.project.fitness.service;

import com.project.fitness.dto.LoginRequest;
import com.project.fitness.dto.RegisterRequest;
import com.project.fitness.dto.RegisterResponse;
import com.project.fitness.entity.AuthProvider;
import com.project.fitness.entity.OtpChannel;
import com.project.fitness.entity.Role;
import com.project.fitness.entity.User;
import com.project.fitness.exception.BadRequestException;
import com.project.fitness.exception.ResourceNotFoundException;
import com.project.fitness.repository.UserRepository;
import com.project.fitness.security.CustomUserDetailsService;
import com.project.fitness.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterDuplicateEmailThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPhoneNumber("1234567890");
        request.setPassword("password");
        request.setFirstName("First");
        request.setLastName("Last");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    void testRegisterDuplicatePhoneThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPhoneNumber("1234567890");
        request.setPassword("password");
        request.setFirstName("First");
        request.setLastName("Last");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPhoneNumber("1234567890");
        request.setPassword("password");
        request.setFirstName("First");
        request.setLastName("Last");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(otpService.createAndSend("new@example.com", OtpChannel.EMAIL)).thenReturn("111111");
        when(otpService.createAndSend("1234567890", OtpChannel.SMS)).thenReturn("222222");

        RegisterResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("new@example.com", response.getEmail());
        assertEquals("111111", response.getEmailOtp());
        assertEquals("222222", response.getSmsOtp());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterWithoutPhoneSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nophone@example.com");
        request.setPassword("password");
        request.setFirstName("No");
        request.setLastName("Phone");

        when(userRepository.existsByEmail("nophone@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(otpService.createAndSend("nophone@example.com", OtpChannel.EMAIL)).thenReturn("333333");

        RegisterResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("nophone@example.com", response.getEmail());
        assertNull(response.getPhoneNumber());
        assertEquals("333333", response.getEmailOtp());
        assertNull(response.getSmsOtp());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLoginUnverifiedUserThrowsBadRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@example.com");
        request.setPassword("password");

        User user = User.builder()
                .id("u1")
                .email("unverified@example.com")
                .phoneNumber("1234567890")
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .phoneVerified(false)
                .build();

        when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    void testGetByIdNotFoundThrowsResourceNotFound() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById("unknown-id"));
    }
}
