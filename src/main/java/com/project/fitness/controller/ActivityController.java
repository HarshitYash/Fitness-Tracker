package com.project.fitness.controller;

import com.project.fitness.dto.*;
import com.project.fitness.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;

    @PostMapping("/user/{userId}")
    public ActivityResponse create(@PathVariable String userId, @Valid @RequestBody ActivityRequest request) {
        return service.create(userId, request);
    }

    @GetMapping("/user/{userId}")
    public List<ActivityResponse> getForUser(@PathVariable String userId) {
        return service.getForUser(userId);
    }

    @GetMapping("/{id}")
    public ActivityResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
