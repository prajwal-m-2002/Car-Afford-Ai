package com.caraffordai.dto;

import lombok.*;

/**
 * UserResponse DTO – Output for user-related API calls.
 * Never expose sensitive or internal fields in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Double monthlyIncome;
    private String message;
}
