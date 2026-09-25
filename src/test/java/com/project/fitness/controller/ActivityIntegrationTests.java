package com.project.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fitness.dto.ActivityRequest;
import com.project.fitness.dto.AuthResponse;
import com.project.fitness.dto.RegisterRequest;
import com.project.fitness.dto.RegisterResponse;
import com.project.fitness.dto.VerifyOtpRequest;
import com.project.fitness.entity.ActivityType;
import com.project.fitness.repository.ActivityRepository;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ActivityIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private String jwtToken;
    private String testUserId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        activityRepository.deleteAll();
        userRepository.deleteAll();

        // Create and verify user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("athlete@example.com");
        registerRequest.setPhoneNumber("9998887776");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Athlete");
        registerRequest.setLastName("One");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        VerifyOtpRequest verifyEmail = new VerifyOtpRequest();
        verifyEmail.setEmail("athlete@example.com");
        verifyEmail.setChannel("EMAIL");
        verifyEmail.setCode(regResponse.getEmailOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmail)));

        VerifyOtpRequest verifySms = new VerifyOtpRequest();
        verifySms.setPhoneNumber("9998887776");
        verifySms.setChannel("SMS");
        verifySms.setCode(regResponse.getSmsOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySms)));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"athlete@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AuthResponse.class);

        jwtToken = authResponse.getToken();
        testUserId = authResponse.getUserId();
    }

    @Test
    void testCreateAndGetActivityFlow() throws Exception {
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setType(ActivityType.RUNNING);
        activityRequest.setDurationMinutes(45);
        activityRequest.setCaloriesBurned(420);
        activityRequest.setStartTime(LocalDateTime.now().minusHours(1));
        activityRequest.setAdditionalMetrics(Map.of("avgHeartRate", 145, "distanceKm", 6.5));

        // Create activity
        MvcResult createResult = mockMvc.perform(post("/api/activities/user/" + testUserId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.type").value("RUNNING"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.caloriesBurned").value(420))
                .andExpect(jsonPath("$.userId").value(testUserId))
                .andReturn();

        String activityId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Get single activity
        mockMvc.perform(get("/api/activities/" + activityId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.type").value("RUNNING"));

        // Get user activities
        mockMvc.perform(get("/api/activities/user/" + testUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUnauthorizedAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/activities/user/" + testUserId))
                .andExpect(status().isForbidden());
    }
}
