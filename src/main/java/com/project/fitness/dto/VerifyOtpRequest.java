package com.project.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String phoneNumber;

    @NotBlank
    private String channel;

    @NotBlank
    private String code;
}
