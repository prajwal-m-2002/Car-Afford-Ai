package com.caraffordai.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * LoginRequest DTO – Used for POST /api/auth/login
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
