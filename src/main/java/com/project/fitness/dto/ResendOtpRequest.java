package com.project.fitness.dto;

import lombok.Data;

@Data
public class ResendOtpRequest {
    private String email;
    private String phoneNumber;
    private String channel;
}
