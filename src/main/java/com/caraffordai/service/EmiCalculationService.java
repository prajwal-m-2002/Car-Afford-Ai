package com.caraffordai.service;

import com.caraffordai.dto.EmiCalculationRequest;
import com.caraffordai.dto.EmiCalculationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EmiCalculationService – Core financial calculation engine.
 *
 * ═══════════════════════════════════════════════════════
 * INTERVIEW EXPLANATION: EMI Formula
 * ═══════════════════════════════════════════════════════
 * EMI = P × r × (1+r)^n / ((1+r)^n - 1)
 *
 * Where:
 * P = Principal (loan amount)
 * r = Monthly interest rate = (annual rate / 12 / 100)
 * n = Loan tenure in months = (years × 12)
 *
 * Example:
 * P = ₹5,00,000 | Annual Rate = 8.5% | Tenure = 5 years (60 months)
 * r = 8.5 / 12 / 100 = 0.007083
 * n = 60
 * EMI = 5,00,000 × 0.007083 × (1.007083)^60 / ((1.007083)^60 - 1)
 * ≈ ₹10,264 per month
 * ═══════════════════════════════════════════════════════
 */
@Service
@Slf4j
public class EmiCalculationService {

    /**
     * Calculate EMI using the standard flat-rate reducing-balance formula.
     *
     * @param request Contains principal, annual interest rate, and tenure
     * @return Full EMI breakdown response
     */
    public EmiCalculationResponse calculateEmi(EmiCalculationRequest request) {
        log.debug("Calculating EMI: principal={}, rate={}, tenure={}yr",
                request.getPrincipal(), request.getAnnualInterestRate(), request.getTenureYears());

        double principal = request.getPrincipal();
        double annualRate = request.getAnnualInterestRate();
        int tenureYears = request.getTenureYears();

        double monthlyEmi = computeMonthlyEmi(principal, annualRate, tenureYears);
        int tenureMonths = tenureYears * 12;
        double totalPayable = monthlyEmi * tenureMonths;
        double totalInterest = totalPayable - principal;

        return EmiCalculationResponse.builder()
                .monthlyEmi(roundToTwo(monthlyEmi))
                .totalPayable(roundToTwo(totalPayable))
                .totalInterest(roundToTwo(totalInterest))
                .principal(roundToTwo(principal))
                .annualInterestRate(annualRate)
                .tenureMonths(tenureMonths)
                .tenureYears(tenureYears)
                .build();
    }

    /**
     * Core EMI formula – reusable by other services.
     *
     * @param principal   Loan amount in INR
     * @param annualRate  Annual interest rate as percentage (e.g., 8.5 for 8.5%)
     * @param tenureYears Loan duration in years
     * @return Monthly EMI in INR
     */
    public double computeMonthlyEmi(double principal, double annualRate, int tenureYears) {
        // Convert annual rate to monthly rate (as decimal)
        double r = annualRate / 12.0 / 100.0;
        // Convert years to months
        int n = tenureYears * 12;

        // Handle 0% interest edge case (rare but possible for subvention schemes)
        if (r == 0) {
            return principal / n;
        }

        // Standard EMI formula: P × r × (1+r)^n / ((1+r)^n - 1)
        double onePlusRPowN = Math.pow(1 + r, n);
        return (principal * r * onePlusRPowN) / (onePlusRPowN - 1);
    }

    /**
     * Calculate maximum loan amount a user can take given their safe EMI budget.
     *
     * Derived from EMI formula by solving for P:
     * P = EMI × ((1+r)^n - 1) / (r × (1+r)^n)
     *
     * @param safeEmi     Maximum monthly EMI the user can afford
     * @param annualRate  Annual interest rate as percentage
     * @param tenureYears Loan tenure in years
     * @return Maximum principal (loan amount)
     */
    public double computeMaxLoanAmount(double safeEmi, double annualRate, int tenureYears) {
        double r = annualRate / 12.0 / 100.0;
        int n = tenureYears * 12;

        if (r == 0) {
            return safeEmi * n;
        }

        // Inverted EMI formula: P = EMI × ((1+r)^n - 1) / (r × (1+r)^n)
        double onePlusRPowN = Math.pow(1 + r, n);
        return safeEmi * (onePlusRPowN - 1) / (r * onePlusRPowN);
    }

    /** Round to 2 decimal places for currency display */
    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
