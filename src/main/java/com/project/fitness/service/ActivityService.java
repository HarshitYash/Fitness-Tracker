package com.project.fitness.service;

import com.project.fitness.dto.*;
import com.project.fitness.entity.Activity;
import com.project.fitness.entity.User;
import com.project.fitness.exception.ResourceNotFoundException;
import com.project.fitness.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserService userService;

    @Transactional
    public ActivityResponse create(String userId, ActivityRequest request) {
        User user = userService.getById(userId);
        Activity activity = Activity.builder()
                .user(user)
                .type(request.getType())
                .durationMinutes(request.getDurationMinutes())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
        return toResponse(activityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getForUser(String userId) {
        userService.getById(userId);
        return activityRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public Activity getEntity(String id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
    }

    public ActivityResponse get(String id) {
        return toResponse(getEntity(id));
    }

    private ActivityResponse toResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId()).userId(a.getUser().getId()).type(a.getType())
                .durationMinutes(a.getDurationMinutes()).caloriesBurned(a.getCaloriesBurned())
                .startTime(a.getStartTime()).additionalMetrics(a.getAdditionalMetrics())
                .createdAt(a.getCreatedAt()).build();
    }
}
