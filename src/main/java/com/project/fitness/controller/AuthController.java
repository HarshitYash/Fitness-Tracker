package com.project.fitness.controller;

import com.project.fitness.dto.*;
import com.project.fitness.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/verify-otp")
    public RegisterResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return userService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    public RegisterResponse resendOtp(@RequestBody ResendOtpRequest request) {
        return userService.resendOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ForgotPasswordResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }

    @PostMapping("/demo-login/{provider}")
    public AuthResponse demoLoginPost(@PathVariable String provider) {
        return userService.socialDemoLogin(provider);
    }

    @GetMapping("/demo-login/{provider}")
    public AuthResponse demoLoginGet(@PathVariable String provider) {
        return userService.socialDemoLogin(provider);
    }

    @GetMapping("/oauth-status")
    public java.util.Map<String, Boolean> oauthStatus(
            @org.springframework.beans.factory.annotation.Value("${app.oauth.google.client-id:}") String googleId,
            @org.springframework.beans.factory.annotation.Value("${app.oauth.google.client-secret:}") String googleSecret,
            @org.springframework.beans.factory.annotation.Value("${app.oauth.github.client-id:}") String githubId,
            @org.springframework.beans.factory.annotation.Value("${app.oauth.github.client-secret:}") String githubSecret) {
        return java.util.Map.of(
                "google", com.project.fitness.config.OAuthClientConfig.isConfigured(googleId, googleSecret),
                "github", com.project.fitness.config.OAuthClientConfig.isConfigured(githubId, githubSecret)
        );
    }
}
