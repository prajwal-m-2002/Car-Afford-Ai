package com.caraffordai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AffordabilityReport Entity – Stores the computed financial report for a user.
 *
 * Interview Tip: This entity is the OUTPUT of our recommendation engine.
 * It ties together:
 * - User's financial profile (via FK)
 * - Computed EMI limits
 * - Stress score (proprietary algorithm)
 * - Recommended car (via FK)
 *
 * The report is immutable once generated (timestamps are set on creation).
 * Future extension: Add PDF export reference field.
 */
@Entity
@Table(name = "affordability_reports", indexes = {
        @Index(name = "idx_report_user_id", columnList = "user_id"),
        @Index(name = "idx_report_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffordabilityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user for whom this report was generated */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Maximum safe EMI the user can afford (INR) */
    @Column(name = "max_emi", nullable = false)
    private Double maxEmi;

    /** Maximum car price the user can afford (loan + down payment) */
    @Column(name = "max_car_price", nullable = false)
    private Double maxCarPrice;

    /**
     * Financial Stress Score (0–100)
     * 0–30 = Safe, 31–60 = Caution, 61–100 = Risky
     */
    @Column(name = "stress_score", nullable = false)
    private Integer stressScore;

    /** Stress level category: SAFE, CAUTION, RISKY */
    @Column(name = "stress_level", nullable = false, length = 20)
    private String stressLevel;

    /** Human-readable explanation of the stress score */
    @Column(name = "stress_explanation", length = 500)
    private String stressExplanation;

    /** The primary recommended car */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_car_id")
    private Car recommendedCar;

    /** Final verdict: BUY or DONT_BUY */
    @Column(nullable = false, length = 20)
    private String verdict;

    /** Advice for budget upgrade or downgrade */
    @Column(name = "upgrade_advice", length = 500)
    private String upgradeAdvice;

    /** Report generation timestamp */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
