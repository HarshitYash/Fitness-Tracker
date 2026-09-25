package com.project.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Email @Size(max = 255)
    private String email;

    @Pattern(regexp = "^$|^[0-9]{10,15}$", message = "Enter a valid mobile number (10-15 digits)")
    private String phoneNumber;

    @NotBlank @Size(min = 8, max = 72)
    private String password;

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;
}
