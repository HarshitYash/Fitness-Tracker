package com.project.fitness.repository;

import com.project.fitness.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, String> {
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Recommendation> findByActivityIdOrderByCreatedAtDesc(String activityId);
}
