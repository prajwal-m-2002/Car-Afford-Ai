package com.caraffordai.service;

import com.caraffordai.entity.Expense;
import com.caraffordai.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * StressScoreService – Proprietary Financial Stress Engine
 *
 * ═══════════════════════════════════════════════════════════════
 * INTERVIEW EXPLANATION: Stress Score Algorithm
 * ═══════════════════════════════════════════════════════════════
 * The stress score (0–100) is a composite metric derived from 3 factors:
 *
 * Factor 1: EMI-to-Income Ratio (Weight: 40%)
 * Measures "how much of your income goes to the car loan"
 * emiRatio = (proposedEmi + existingEmi) / monthlyIncome
 * Score: emiRatio > 0.5 → high stress; < 0.2 → low stress
 *
 * Factor 2: Savings Adequacy (Weight: 35%)
 * After paying EMI, expenses, can you save anything?
 * monthlySavings = income - expenses - existingEmi - proposedEmi
 * Score: savings < 0 → maximum stress; > 30% income → low stress
 *
 * Factor 3: Existing Debt Burden (Weight: 25%)
 * Pre-existing EMI as % of income adds to financial fragility
 * existingDebtRatio = existingEmi / monthlyIncome
 * Score: > 30% → high stress
 *
 * Final Score = weighted average of all 3 factors (clamped 0–100)
 *
 * Interpretation:
 * 0–30 → SAFE ✅ – Healthy financial position
 * 31–60 → CAUTION ⚠️ – Manageable but tight
 * 61–100 → RISKY ❌ – Financial strain likely
 * ═══════════════════════════════════════════════════════════════
 */
@Service
@Slf4j
public class StressScoreService {

    // Factor weights (must sum to 1.0)
    private static final double WEIGHT_EMI_RATIO = 0.40;
    private static final double WEIGHT_SAVINGS = 0.35;
    private static final double WEIGHT_EXISTING_DEBT = 0.25;

    /**
     * Calculate the overall financial stress score.
     *
     * @param user        The user entity (for income)
     * @param expense     The expense entity (for existing obligations)
     * @param proposedEmi The monthly EMI for the proposed car loan
     * @return StressResult (score + level + explanation)
     */
    public StressResult calculateStressScore(User user, Expense expense, double proposedEmi) {
        double income = user.getMonthlyIncome();
        double fixedExpenses = expense.getFixedExpenses();
        double existingEmi = expense.getExistingEmi();

        log.debug("Calculating stress: income={}, fixedExp={}, existingEmi={}, proposedEmi={}",
                income, fixedExpenses, existingEmi, proposedEmi);

        // ── Factor 1: EMI-to-Income Ratio ────────────────────────────────────
        double totalEmi = existingEmi + proposedEmi;
        double emiRatio = totalEmi / income; // 0 = best, 1+ = worst
        // Normalize to 0–100: ratio 0.5+ = 100 score
        double emiScore = Math.min(emiRatio / 0.5, 1.0) * 100;

        // ── Factor 2: Savings Adequacy ───────────────────────────────────────
        double monthlySavings = income - fixedExpenses - existingEmi - proposedEmi;
        // Normalize: savings as % of income (negative = worst, 30%+ = best)
        double savingsRatio = monthlySavings / income; // can be negative
        // Higher savings = lower stress. Invert: (0 savings → 100, 30%+ savings → 0)
        double savingsScore = Math.max(0, Math.min(1.0, (0.30 - savingsRatio) / 0.30)) * 100;

        // ── Factor 3: Existing Debt Burden ───────────────────────────────────
        double existingDebtRatio = existingEmi / income;
        // Normalize: debtRatio 0.30+ = 100 stress score
        double debtScore = Math.min(existingDebtRatio / 0.30, 1.0) * 100;

        // ── Weighted Final Score ──────────────────────────────────────────────
        double rawScore = (emiScore * WEIGHT_EMI_RATIO)
                + (savingsScore * WEIGHT_SAVINGS)
                + (debtScore * WEIGHT_EXISTING_DEBT);

        int finalScore = (int) Math.round(Math.min(100, Math.max(0, rawScore)));

        // ── Classification ────────────────────────────────────────────────────
        String level;
        String explanation;

        if (finalScore <= 30) {
            level = "SAFE";
            explanation = buildSafeExplanation(emiRatio, monthlySavings, income);
        } else if (finalScore <= 60) {
            level = "CAUTION";
            explanation = buildCautionExplanation(emiRatio, monthlySavings, income);
        } else {
            level = "RISKY";
            explanation = buildRiskyExplanation(emiRatio, monthlySavings, income);
        }

        log.info("Stress Score: {} | Level: {} | EMI%: {}% | Savings: ₹{}",
                finalScore, level, String.format("%.1f", emiRatio * 100), Math.round(monthlySavings));

        return new StressResult(finalScore, level, explanation, monthlySavings);
    }

    // ─── Explanation Builders ─────────────────────────────────────────────────

    private String buildSafeExplanation(double emiRatio, double savings, double income) {
        return String.format(
                "✅ SAFE ZONE: Your total EMI commitment is only %.1f%% of your income. " +
                        "You will have ₹%.0f left after all payments — a healthy savings cushion. " +
                        "This car won't strain your finances.",
                emiRatio * 100, savings);
    }

    private String buildCautionExplanation(double emiRatio, double savings, double income) {
        return String.format(
                "⚠️ CAUTION ZONE: Your total EMI is %.1f%% of your income. " +
                        "You will have ₹%.0f remaining per month — manageable, but tight. " +
                        "Consider a shorter tenure or higher down payment to reduce EMI.",
                emiRatio * 100, savings);
    }

    private String buildRiskyExplanation(double emiRatio, double savings, double income) {
        String savingsMsg = savings < 0
                ? String.format("You'll be short by ₹%.0f every month — a financial deficit!", Math.abs(savings))
                : String.format("Only ₹%.0f will remain after payments — dangerously thin!", savings);

        return String.format(
                "❌ RISKY ZONE: Your total EMI is %.1f%% of your income. %s " +
                        "We strongly recommend a cheaper car, larger down payment, or clearing existing loans first.",
                emiRatio * 100, savingsMsg);
    }

    // ─── Inner Result Class ───────────────────────────────────────────────────

    /**
     * StressResult is a simple value object (record-like) to return multiple
     * values.
     * Could also be a Java 16+ record, but using class for Java 21 compatibility
     * with Lombok.
     */
    public static class StressResult {
        public final int score;
        public final String level; // SAFE | CAUTION | RISKY
        public final String explanation;
        public final double monthlySavings;

        public StressResult(int score, String level, String explanation, double monthlySavings) {
            this.score = score;
            this.level = level;
            this.explanation = explanation;
            this.monthlySavings = monthlySavings;
        }
    }
}
