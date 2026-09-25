package com.project.fitness.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Fitness Monolith API",
        version = "1.0",
        description = "REST API for users, fitness activities and recommendations"
))
public class OpenApiConfig {
}
