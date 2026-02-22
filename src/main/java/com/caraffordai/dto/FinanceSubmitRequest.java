package com.caraffordai.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * FinanceSubmitRequest DTO – Input for POST /api/finance/submit
 *
 * Contains the user's complete financial profile needed to:
 * 1. Calculate Safe EMI
 * 2. Determine Max Car Price
 * 3. Compute Stress Score
 *
 * Interview Tip: The preferred tenure is used to find the best loan option
 * from our loan_options table. We calculate EMI for multiple tenures
 * and recommend the one that gives the healthiest stress score.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceSubmitRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Fixed monthly expenses are required")
    @Min(value = 0, message = "Fixed expenses cannot be negative")
    private Double fixedExpenses;

    @NotNull(message = "Existing EMI amount is required")
    @Min(value = 0, message = "Existing EMI cannot be negative")
    private Double existingEmi;

    @NotNull(message = "Down payment is required")
    @Min(value = 0, message = "Down payment cannot be negative")
    private Double downPayment;

    @NotNull(message = "Preferred loan tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 year")
    @Max(value = 7, message = "Tenure cannot exceed 7 years")
    private Integer preferredTenureYears;
}
