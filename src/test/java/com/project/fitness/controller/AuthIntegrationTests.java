package com.project.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fitness.dto.*;
import com.project.fitness.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    void testRegistrationVerificationAndLoginFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setPassword("securePassword123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        // 1. Register
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.phoneVerified").value(false))
                .andExpect(jsonPath("$.emailOtp").isNotEmpty())
                .andExpect(jsonPath("$.smsOtp").isNotEmpty())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        // 2. Try Login before verification - Should fail
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("securePassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // 3. Verify Email OTP
        VerifyOtpRequest verifyEmailReq = new VerifyOtpRequest();
        verifyEmailReq.setEmail("john.doe@example.com");
        verifyEmailReq.setChannel("EMAIL");
        verifyEmailReq.setCode(regResponse.getEmailOtp());

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyEmailReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));

        // 4. Verify SMS OTP
        VerifyOtpRequest verifySmsReq = new VerifyOtpRequest();
        verifySmsReq.setPhoneNumber("1234567890");
        verifySmsReq.setChannel("SMS");
        verifySmsReq.setCode(regResponse.getSmsOtp());

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifySmsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneVerified").value(true));

        // 5. Login successfully
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testRegisterWithoutPhoneFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("optional.phone@example.com");
        registerRequest.setPassword("securePassword123");
        registerRequest.setFirstName("No");
        registerRequest.setLastName("Phone");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("optional.phone@example.com"))
                .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.emailOtp").isNotEmpty())
                .andExpect(jsonPath("$.smsOtp").doesNotExist())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        // Verify only Email OTP
        VerifyOtpRequest verifyEmailReq = new VerifyOtpRequest();
        verifyEmailReq.setEmail("optional.phone@example.com");
        verifyEmailReq.setChannel("EMAIL");
        verifyEmailReq.setCode(regResponse.getEmailOtp());

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyEmailReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));

        // Login directly without SMS OTP
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("optional.phone@example.com");
        loginRequest.setPassword("securePassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testForgotPasswordAndResetFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("reset.user@example.com");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setPassword("oldPassword123");
        registerRequest.setFirstName("Reset");
        registerRequest.setLastName("User");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        // Verify user first
        VerifyOtpRequest verifyEmail = new VerifyOtpRequest();
        verifyEmail.setEmail("reset.user@example.com");
        verifyEmail.setChannel("EMAIL");
        verifyEmail.setCode(regResponse.getEmailOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmail)));

        VerifyOtpRequest verifySms = new VerifyOtpRequest();
        verifySms.setPhoneNumber("9876543210");
        verifySms.setChannel("SMS");
        verifySms.setCode(regResponse.getSmsOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySms)));

        // Forgot password
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest();
        forgotReq.setEmail("reset.user@example.com");

        MvcResult forgotResult = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otp").isNotEmpty())
                .andReturn();

        ForgotPasswordResponse forgotResponse = objectMapper.readValue(
                forgotResult.getResponse().getContentAsString(), ForgotPasswordResponse.class);

        // Reset password
        ResetPasswordRequest resetReq = new ResetPasswordRequest();
        resetReq.setEmail("reset.user@example.com");
        resetReq.setCode(forgotResponse.getOtp());
        resetReq.setNewPassword("newPassword456");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk());

        // Login with new password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("reset.user@example.com");
        loginRequest.setPassword("newPassword456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testOAuthStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/oauth-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.google").isBoolean())
                .andExpect(jsonPath("$.github").isBoolean());
    }
}
