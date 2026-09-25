package com.project.fitness.dto;

import com.project.fitness.entity.ActivityType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityRequest {
    @NotNull
    private ActivityType type;

    @NotNull @Positive
    private Integer durationMinutes;

    @NotNull @PositiveOrZero
    private Integer caloriesBurned;

    @NotNull
    private LocalDateTime startTime;

    private Map<String, Object> additionalMetrics;
}
