package com.project.fitness.dto;

import com.project.fitness.entity.RecommendationType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class RecommendationRequest {
    @NotBlank @Size(max = 2000)
    private String recommendation;

    @NotNull
    private RecommendationType type;

    private List<String> improvements;
    private List<String> suggestions;
    private List<String> safety;

    @NotBlank
    private String activityId;
}
