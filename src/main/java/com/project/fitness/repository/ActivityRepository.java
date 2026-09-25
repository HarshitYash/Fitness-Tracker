package com.project.fitness.repository;

import com.project.fitness.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserIdOrderByStartTimeDesc(String userId);
}
