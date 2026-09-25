package com.project.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fitness.dto.AuthResponse;
import com.project.fitness.dto.RegisterRequest;
import com.project.fitness.dto.RegisterResponse;
import com.project.fitness.dto.VerifyOtpRequest;
import com.project.fitness.entity.Role;
import com.project.fitness.entity.User;
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
class UserIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    void testGetUserByIdAndAdminGetAllUsers() throws Exception {
        // Register regular user
        RegisterRequest registerUser = new RegisterRequest();
        registerUser.setEmail("member@example.com");
        registerUser.setPhoneNumber("5554443332");
        registerUser.setPassword("password123");
        registerUser.setFirstName("Regular");
        registerUser.setLastName("Member");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        // Verify regular user
        VerifyOtpRequest verifyEmail = new VerifyOtpRequest();
        verifyEmail.setEmail("member@example.com");
        verifyEmail.setChannel("EMAIL");
        verifyEmail.setCode(regResponse.getEmailOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmail)));

        VerifyOtpRequest verifySms = new VerifyOtpRequest();
        verifySms.setPhoneNumber("5554443332");
        verifySms.setChannel("SMS");
        verifySms.setCode(regResponse.getSmsOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySms)));

        // Login as regular user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse userAuth = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AuthResponse.class);

        // 1. Get user by ID with valid token
        mockMvc.perform(get("/api/users/" + userAuth.getUserId())
                        .header("Authorization", "Bearer " + userAuth.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userAuth.getUserId()))
                .andExpect(jsonPath("$.email").value("member@example.com"));

        // 2. Regular user cannot access admin list endpoint
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userAuth.getToken()))
                .andExpect(status().isForbidden());

        // 3. Elevate user to ADMIN and test admin access
        User dbUser = userRepository.findById(userAuth.getUserId()).orElseThrow();
        dbUser.setRole(Role.ADMIN);
        userRepository.save(dbUser);

        // Re-login to get updated JWT with ADMIN role
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse adminAuth = objectMapper.readValue(
                adminLoginResult.getResponse().getContentAsString(), AuthResponse.class);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminAuth.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
