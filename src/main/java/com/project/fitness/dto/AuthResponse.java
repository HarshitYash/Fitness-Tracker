package com.project.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}
