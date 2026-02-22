package com.caraffordai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Expense Entity – Stores the user's financial obligations.
 *
 * Interview Tip: This is a OneToOne mapping with User.
 * We store expenses separately to maintain SRP (Single Responsibility
 * Principle)
 * and allow future extension of expense categories.
 *
 * Business Rule:
 * Safe EMI = (monthlyIncome - fixedExpenses) × 0.40
 * Existing EMI reduces the safe EMI budget further.
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expenses_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Foreign key to users table */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Total fixed monthly expenses (rent, bills, groceries, etc.) */
    @Column(name = "fixed_expenses", nullable = false)
    private Double fixedExpenses;

    /** Sum of all existing loan EMIs the user is already paying */
    @Column(name = "existing_emi", nullable = false)
    private Double existingEmi;

    /** Amount the user can pay upfront as down payment */
    @Column(name = "down_payment", nullable = false)
    private Double downPayment;

    /** Preferred loan tenure in years (3, 5, or 7) */
    @Column(name = "preferred_tenure_years", nullable = false)
    private Integer preferredTenureYears;
}
