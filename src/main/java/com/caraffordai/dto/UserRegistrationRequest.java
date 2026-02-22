package com.caraffordai.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * UserRegistrationRequest DTO – Input for POST /api/auth/register
 *
 * Interview Tip: DTOs decouple API from entity, allow validation,
 * and prevent over-posting (mass assignment attacks).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Monthly income is required")
    @Min(value = 10000, message = "Monthly income must be at least ₹10,000")
    @Max(value = 10000000, message = "Monthly income must be realistic (max ₹1 Crore)")
    private Double monthlyIncome;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6–100 characters")
    private String password;
}
