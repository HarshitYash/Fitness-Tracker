package com.project.fitness.service;

import com.project.fitness.dto.*;
import com.project.fitness.entity.AuthProvider;
import com.project.fitness.entity.OtpChannel;
import com.project.fitness.entity.Role;
import com.project.fitness.entity.User;
import com.project.fitness.exception.BadRequestException;
import com.project.fitness.exception.ResourceNotFoundException;
import com.project.fitness.repository.UserRepository;
import com.project.fitness.security.CustomUserDetailsService;
import com.project.fitness.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String phone = normalizePhone(request.getPhoneNumber());

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }
        if (phone != null && userRepository.existsByPhoneNumber(phone)) {
            throw new BadRequestException("Mobile number is already registered");
        }

        User user = User.builder()
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .phoneVerified(phone == null)
                .build();
        userRepository.save(user);

        String emailOtp = otpService.createAndSend(email, OtpChannel.EMAIL);
        String smsOtp = phone != null ? otpService.createAndSend(phone, OtpChannel.SMS) : null;

        String message = phone != null
                ? "Account created. Verify the OTPs sent to your email and mobile number."
                : "Account created. Verify the OTP sent to your email.";

        return RegisterResponse.builder()
                .message(message)
                .email(email)
                .phoneNumber(phone)
                .emailVerified(false)
                .phoneVerified(phone == null)
                .emailOtp(emailOtp)
                .smsOtp(smsOtp)
                .build();
    }

    @Transactional
    public RegisterResponse verifyOtp(VerifyOtpRequest request) {
        OtpChannel channel = parseChannel(request.getChannel());
        if (channel == OtpChannel.EMAIL) {
            String email = requireEmail(request.getEmail());
            User user = getByEmail(email);
            otpService.verify(email, OtpChannel.EMAIL, request.getCode());
            user.setEmailVerified(true);
            userRepository.save(user);
            return status(user, "Email verified successfully.");
        }

        String phone = normalizePhone(requirePhone(request.getPhoneNumber()));
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this mobile number"));
        otpService.verify(phone, OtpChannel.SMS, request.getCode());
        user.setPhoneVerified(true);
        userRepository.save(user);
        return status(user, "Mobile number verified successfully.");
    }

    @Transactional
    public RegisterResponse resendOtp(ResendOtpRequest request) {
        OtpChannel channel = parseChannel(request.getChannel());
        if (channel == OtpChannel.EMAIL) {
            String email = requireEmail(request.getEmail());
            User user = getByEmail(email);
            if (user.isEmailVerified()) {
                throw new BadRequestException("Email is already verified");
            }
            String code = otpService.createAndSend(email, OtpChannel.EMAIL);
            return RegisterResponse.builder()
                    .message("A new email OTP has been sent.")
                    .email(email)
                    .phoneNumber(user.getPhoneNumber())
                    .emailVerified(user.isEmailVerified())
                    .phoneVerified(user.isPhoneVerified())
                    .emailOtp(code)
                    .build();
        }

        String phone = normalizePhone(requirePhone(request.getPhoneNumber()));
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this mobile number"));
        if (user.isPhoneVerified()) {
            throw new BadRequestException("Mobile number is already verified");
        }
        String code = otpService.createAndSend(phone, OtpChannel.SMS);
        return RegisterResponse.builder()
                .message("A new SMS OTP has been sent.")
                .email(user.getEmail())
                .phoneNumber(phone)
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .smsOtp(code)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = getByEmail(request.getEmail());
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("This account uses " + user.getAuthProvider()
                    + " login. Please sign in with that provider.");
        }
        if (!user.isFullyVerified()) {
            throw new BadRequestException("Please verify your email and mobile number before signing in.");
        }
        var details = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.builder()
                .token(jwtService.generateToken(details))
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build();
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = requireEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this email"));
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("This account uses " + user.getAuthProvider()
                    + " sign-in. Reset the password with that provider, or sign in with Google/GitHub.");
        }
        String otp = otpService.createAndSend(email, OtpChannel.PASSWORD_RESET);
        return ForgotPasswordResponse.builder()
                .message("A password reset OTP has been sent to your email.")
                .email(email)
                .otp(otp)
                .build();
    }

    @Transactional
    public ForgotPasswordResponse resetPassword(ResetPasswordRequest request) {
        String email = requireEmail(request.getEmail());
        User user = getByEmail(email);
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("This account uses social login. Password reset is not available.");
        }
        otpService.verify(email, OtpChannel.PASSWORD_RESET, request.getCode());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setEmailVerified(true);
        userRepository.save(user);
        return ForgotPasswordResponse.builder()
                .message("Password updated. You can sign in with your new password.")
                .email(email)
                .build();
    }

    @Transactional
    public AuthResponse socialDemoLogin(String provider) {
        String p = provider.trim().toLowerCase();
        AuthProvider authProvider;
        String email;
        String firstName;
        String lastName;

        if ("google".equals(p)) {
            authProvider = AuthProvider.GOOGLE;
            email = "alex.google@gmail.com";
            firstName = "Alex";
            lastName = "Google";
        } else if ("github".equals(p)) {
            authProvider = AuthProvider.GITHUB;
            email = "octocat.github@github.com";
            firstName = "Octocat";
            lastName = "GitHub";
        } else {
            throw new BadRequestException("Unsupported provider: " + provider);
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.USER)
                .authProvider(authProvider)
                .emailVerified(true)
                .phoneVerified(false)
                .build()));

        var details = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.builder()
                .token(jwtService.generateToken(details))
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build();
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    private RegisterResponse status(User user, String message) {
        return RegisterResponse.builder()
                .message(message)
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build();
    }

    private OtpChannel parseChannel(String channel) {
        try {
            return OtpChannel.valueOf(channel.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Channel must be EMAIL or SMS");
        }
    }

    private String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return email.toLowerCase().trim();
    }

    private String requirePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("Mobile number is required");
        }
        return phone;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
