package com.project.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fitness.dto.*;
import com.project.fitness.entity.ActivityType;
import com.project.fitness.entity.RecommendationType;
import com.project.fitness.repository.ActivityRepository;
import com.project.fitness.repository.RecommendationRepository;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RecommendationIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    private String jwtToken;
    private String testUserId;
    private String testActivityId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        recommendationRepository.deleteAll();
        activityRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("coach@example.com");
        registerRequest.setPhoneNumber("1112223334");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Coach");
        registerRequest.setLastName("Trainer");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        RegisterResponse regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), RegisterResponse.class);

        VerifyOtpRequest verifyEmail = new VerifyOtpRequest();
        verifyEmail.setEmail("coach@example.com");
        verifyEmail.setChannel("EMAIL");
        verifyEmail.setCode(regResponse.getEmailOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmail)));

        VerifyOtpRequest verifySms = new VerifyOtpRequest();
        verifySms.setPhoneNumber("1112223334");
        verifySms.setChannel("SMS");
        verifySms.setCode(regResponse.getSmsOtp());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifySms)));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"coach@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AuthResponse.class);

        jwtToken = authResponse.getToken();
        testUserId = authResponse.getUserId();

        // Create an activity
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setType(ActivityType.CYCLING);
        activityRequest.setDurationMinutes(60);
        activityRequest.setCaloriesBurned(500);
        activityRequest.setStartTime(LocalDateTime.now().minusHours(2));

        MvcResult activityResult = mockMvc.perform(post("/api/activities/user/" + testUserId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activityRequest)))
                .andExpect(status().isOk())
                .andReturn();

        testActivityId = objectMapper.readTree(activityResult.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void testCreateAndGetRecommendations() throws Exception {
        RecommendationRequest recRequest = new RecommendationRequest();
        recRequest.setActivityId(testActivityId);
        recRequest.setType(RecommendationType.RECOMMENDATION);
        recRequest.setRecommendation("Great tempo, keep cadence above 85 RPM.");
        recRequest.setImprovements(List.of("Cadence consistency", "Hydration"));
        recRequest.setSuggestions(List.of("Spin high gear on flats"));
        recRequest.setSafety(List.of("Helmet check"));

        // Create recommendation
        mockMvc.perform(post("/api/recommendations/user/" + testUserId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.recommendation").value("Great tempo, keep cadence above 85 RPM."))
                .andExpect(jsonPath("$.improvements[0]").value("Cadence consistency"));

        // Get recommendations for user
        mockMvc.perform(get("/api/recommendations/user/" + testUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
