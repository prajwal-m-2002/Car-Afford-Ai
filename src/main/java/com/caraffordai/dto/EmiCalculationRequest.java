package com.caraffordai.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * EmiCalculationRequest DTO – Input for POST /api/emi/calculate
 *
 * Can be used standalone (without registering a user) for quick EMI checks.
 *
 * Interview Tip: We separate the EMI calculation endpoint from the full
 * recommendation flow so it can be used as a utility widget independently.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiCalculationRequest {

    @NotNull(message = "Loan principal amount is required")
    @Min(value = 50000, message = "Loan amount must be at least ₹50,000")
    private Double principal;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "1.0", message = "Interest rate must be at least 1%")
    @DecimalMax(value = "30.0", message = "Interest rate cannot exceed 30%")
    private Double annualInterestRate;

    @NotNull(message = "Tenure in years is required")
    @Min(value = 1, message = "Tenure must be at least 1 year")
    @Max(value = 7, message = "Tenure cannot exceed 7 years")
    private Integer tenureYears;
}
