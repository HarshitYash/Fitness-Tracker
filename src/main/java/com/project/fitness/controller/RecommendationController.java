package com.project.fitness.controller;

import com.project.fitness.dto.*;
import com.project.fitness.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService service;

    @PostMapping("/user/{userId}")
    public RecommendationResponse create(@PathVariable String userId,
                                         @Valid @RequestBody RecommendationRequest request) {
        return service.create(userId, request);
    }

    @GetMapping("/user/{userId}")
    public List<RecommendationResponse> getForUser(@PathVariable String userId) {
        return service.getForUser(userId);
    }
}
