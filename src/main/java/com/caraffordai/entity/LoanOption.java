package com.caraffordai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * LoanOption Entity – Predefined loan interest rates by tenure.
 *
 * Interview Tip: Instead of hard-coding interest rates, we store them in DB.
 * This enables bank-wise comparison in the future (Extension Point).
 *
 * The EMI calculation uses:
 * r = (annual interest rate / 12 / 100)
 * n = tenure_years * 12
 */
@Entity
@Table(name = "loan_options", indexes = {
        @Index(name = "idx_loan_tenure", columnList = "tenure_years")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bank or lender name – for future bank comparison feature */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /** Annual interest rate as a percentage (e.g., 8.5 for 8.5%) */
    @Column(name = "interest_rate", nullable = false)
    private Double interestRate;

    /** Loan tenure in years (typically 3, 5, or 7) */
    @Column(name = "tenure_years", nullable = false)
    private Integer tenureYears;
}
