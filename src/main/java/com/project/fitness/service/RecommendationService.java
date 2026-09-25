package com.project.fitness.service;

import com.project.fitness.dto.*;
import com.project.fitness.entity.*;
import com.project.fitness.exception.ResourceNotFoundException;
import com.project.fitness.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository repository;
    private final UserService userService;
    private final ActivityService activityService;

    @Transactional
    public RecommendationResponse create(String userId, RecommendationRequest request) {
        User user = userService.getById(userId);
        Activity activity = activityService.getEntity(request.getActivityId());
        if (!activity.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Activity does not belong to this user");
        }
        Recommendation r = Recommendation.builder()
                .user(user).activity(activity).type(request.getType())
                .recommendation(request.getRecommendation())
                .improvements(request.getImprovements())
                .suggestions(request.getSuggestions())
                .safety(request.getSafety())
                .build();
        return toResponse(repository.save(r));
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getForUser(String userId) {
        userService.getById(userId);
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    private RecommendationResponse toResponse(Recommendation r) {
        return RecommendationResponse.builder()
                .id(r.getId()).userId(r.getUser().getId()).activityId(r.getActivity().getId())
                .type(r.getType()).recommendation(r.getRecommendation())
                .improvements(r.getImprovements()).suggestions(r.getSuggestions())
                .safety(r.getSafety()).createdAt(r.getCreatedAt()).build();
    }
}
