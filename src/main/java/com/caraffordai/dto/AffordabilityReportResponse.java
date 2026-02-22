package com.caraffordai.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AffordabilityReportResponse DTO – Full report output for GET
 * /api/report/{userId}
 *
 * This is the comprehensive summary shown on the Report/Download screen.
 * It includes:
 * - User income snapshot
 * - EMI breakdown
 * - Stress score + explanation
 * - Top 3 recommended cars
 * - Verdict: BUY / DONT_BUY
 * - Upgrade/Downgrade advice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffordabilityReportResponse {

    // ─── Report Metadata ────────────────────────────────────
    private Long reportId;
    private LocalDateTime generatedAt;

    // ─── User Snapshot ──────────────────────────────────────
    private String userName;
    private String userEmail;
    private Double monthlyIncome;

    // ─── Financial Input Summary ────────────────────────────
    private Double fixedExpenses;
    private Double existingEmi;
    private Double downPayment;
    private Integer tenureYears;

    // ─── Computed Values ────────────────────────────────────
    /** Safe EMI = (income - expenses) × 0.40 */
    private Double safeEmi;

    /** Maximum loan amount user can take */
    private Double maxLoanAmount;

    /** Max affordable car price (loan + down payment) */
    private Double maxCarPrice;

    /** Net savings per month after all commitments */
    private Double monthlySavings;

    // ─── Stress Score Engine ────────────────────────────────
    private Integer stressScore;

    /** SAFE | CAUTION | RISKY */
    private String stressLevel;

    private String stressExplanation;

    // ─── Car Recommendations ─────────────────────────────────
    private List<CarRecommendationResponse> recommendedCars;

    // ─── Verdict ────────────────────────────────────────────
    /** BUY | DONT_BUY */
    private String verdict;

    private String upgradeAdvice;
}
