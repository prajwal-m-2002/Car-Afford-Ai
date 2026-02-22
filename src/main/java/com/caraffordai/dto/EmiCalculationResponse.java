package com.caraffordai.dto;

import lombok.*;

/**
 * EmiCalculationResponse DTO – Output for EMI calculation APIs.
 *
 * Contains all computed values needed to populate the EMI Dashboard screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiCalculationResponse {

    /** Monthly EMI amount (INR) */
    private Double monthlyEmi;

    /** Total amount paid over the full tenure */
    private Double totalPayable;

    /** Total interest cost */
    private Double totalInterest;

    /** Loan principal */
    private Double principal;

    /** Annual interest rate used */
    private Double annualInterestRate;

    /** Tenure in months */
    private Integer tenureMonths;

    /** Tenure in years */
    private Integer tenureYears;
}
