package com.caraffordai.service;

import com.caraffordai.dto.*;
import com.caraffordai.entity.*;
import com.caraffordai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * RecommendationService – The Heart of CarAfford AI
 *
 * ═══════════════════════════════════════════════════════════════
 * INTERVIEW EXPLANATION: Recommendation Algorithm
 * ═══════════════════════════════════════════════════════════════
 * Step 1: Retrieve user's income and expense profile
 * Step 2: Calculate Safe EMI = (income - fixedExpenses) × 0.40
 * (then subtract existingEmi to get remaining EMI budget)
 * Step 3: Find best loan option from DB (lowest rate for tenure)
 * Step 4: Calculate Max Loan Amount using inverted EMI formula
 * Step 5: Max Car Price = maxLoanAmount + downPayment
 * Step 6: Query DB for cars priced ≤ maxCarPrice
 * Step 7: For each car, compute: EMI, ownership cost, income%
 * Step 8: Calculate stress score for each recommendation
 * Step 9: Save report and return top N cars
 * ═══════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommendationService {

        private final UserService userService;
        private final EmiCalculationService emiCalculationService;
        private final StressScoreService stressScoreService;
        private final CarRepository carRepository;
        private final LoanOptionRepository loanOptionRepository;
        private final AffordabilityReportRepository reportRepository;

        /** Safety ratio from config (default 0.40) */
        @Value("${carafford.emi.safety-ratio:0.40}")
        private double emiSafetyRatio;

        /** Default fallback interest rate if no DB entry found */
        @Value("${carafford.loan.default-interest-rate:8.5}")
        private double defaultInterestRate;

        /** Number of top cars to recommend */
        @Value("${carafford.recommendation.top-n:3}")
        private int topN;

        // ─── Main Recommendation Orchestrator ────────────────────────────────────

        /**
         * Generate the full affordability report with car recommendations.
         *
         * @param userId The user ID to generate the report for
         * @return Complete affordability report
         */
        @Transactional
        public AffordabilityReportResponse generateReport(Long userId) {
                log.info("Generating affordability report for user ID: {}", userId);

                // Step 1: Fetch user and expense profiles
                User user = userService.getUserById(userId);
                Expense expense = userService.getExpenseByUserId(userId);

                double income = user.getMonthlyIncome();
                double fixedExpenses = expense.getFixedExpenses();
                double existingEmi = expense.getExistingEmi();
                double downPayment = expense.getDownPayment();
                int tenureYears = expense.getPreferredTenureYears();

                // Step 2: Calculate Safe EMI
                // Safe EMI = (income - fixedExpenses) × emiSafetyRatio, minus existing EMIs
                double grossSafeEmi = (income - fixedExpenses) * emiSafetyRatio;
                double safeEmi = Math.max(0, grossSafeEmi - existingEmi);
                log.debug("Safe EMI: gross={}, net after existing={}", grossSafeEmi, safeEmi);

                // Step 3: Find best interest rate for the preferred tenure
                double interestRate = loanOptionRepository
                                .findFirstByTenureYearsOrderByInterestRateAsc(tenureYears)
                                .map(LoanOption::getInterestRate)
                                .orElse(defaultInterestRate);
                log.debug("Using interest rate: {}% for {} year tenure", interestRate, tenureYears);

                // Step 4: Calculate max loan amount
                double maxLoanAmount = emiCalculationService.computeMaxLoanAmount(safeEmi, interestRate, tenureYears);

                // Step 5: Max affordable car price
                double maxCarPrice = maxLoanAmount + downPayment;
                log.info("Max Car Price: ₹{} (Loan: ₹{} + Down Payment: ₹{})", maxCarPrice, maxLoanAmount, downPayment);

                // Step 6: Query cars within budget
                List<Car> affordableCars = carRepository.findAffordableCars(maxCarPrice);
                log.info("Found {} cars within budget", affordableCars.size());

                // Step 7 & 8: Build recommendation cards for top N cars
                List<CarRecommendationResponse> recommendations = buildRecommendations(
                                affordableCars, expense, user, interestRate, tenureYears, income);

                // Step 8: Calculate stress score using the top recommended car's EMI (if any)
                double representativeEmi = recommendations.isEmpty() ? safeEmi
                                : recommendations.get(0).getMonthlyEmi();

                StressScoreService.StressResult stressResult = stressScoreService.calculateStressScore(user, expense,
                                representativeEmi);

                // Step 9: Verdict
                String verdict = determineVerdict(stressResult, recommendations);
                String upgradeAdvice = buildUpgradeAdvice(stressResult, maxCarPrice, affordableCars);

                // Step 10: Save report to DB
                Car topCar = recommendations.isEmpty() ? null
                                : carRepository.findById(recommendations.get(0).getCarId()).orElse(null);

                AffordabilityReport report = AffordabilityReport.builder()
                                .user(user)
                                .maxEmi(safeEmi)
                                .maxCarPrice(maxCarPrice)
                                .stressScore(stressResult.score)
                                .stressLevel(stressResult.level)
                                .stressExplanation(stressResult.explanation)
                                .recommendedCar(topCar)
                                .verdict(verdict)
                                .upgradeAdvice(upgradeAdvice)
                                .build();

                AffordabilityReport savedReport = reportRepository.save(report);

                // Build and return the response
                return AffordabilityReportResponse.builder()
                                .reportId(savedReport.getId())
                                .generatedAt(savedReport.getCreatedAt())
                                .userName(user.getName())
                                .userEmail(user.getEmail())
                                .monthlyIncome(income)
                                .fixedExpenses(fixedExpenses)
                                .existingEmi(existingEmi)
                                .downPayment(downPayment)
                                .tenureYears(tenureYears)
                                .safeEmi(round(safeEmi))
                                .maxLoanAmount(round(maxLoanAmount))
                                .maxCarPrice(round(maxCarPrice))
                                .monthlySavings(round(stressResult.monthlySavings))
                                .stressScore(stressResult.score)
                                .stressLevel(stressResult.level)
                                .stressExplanation(stressResult.explanation)
                                .recommendedCars(recommendations)
                                .verdict(verdict)
                                .upgradeAdvice(upgradeAdvice)
                                .build();
        }

        // ─── Car Recommendation Builder ───────────────────────────────────────────

        private List<CarRecommendationResponse> buildRecommendations(
                        List<Car> cars, Expense expense, User user,
                        double interestRate, int tenureYears, double income) {

                List<CarRecommendationResponse> result = new ArrayList<>();
                int rank = 1;

                for (Car car : cars) {
                        if (rank > topN)
                                break;

                        // Loan amount = car price - down payment
                        double loanAmount = Math.max(0, car.getPrice() - expense.getDownPayment());

                        // Calculate EMI for this car
                        double emi = emiCalculationService.computeMonthlyEmi(loanAmount, interestRate, tenureYears);

                        // Total monthly ownership cost = EMI + maintenance
                        double totalMonthlyCost = emi + car.getMaintenanceCost();

                        // What % of income does this car consume?
                        double incomeUsagePercent = (totalMonthlyCost / income) * 100;

                        // Why recommended text
                        String whyRecommended = buildWhyRecommended(car, rank, emi, incomeUsagePercent,
                                        expense.getDownPayment());

                        result.add(CarRecommendationResponse.builder()
                                        .carId(car.getId())
                                        .brand(car.getBrand())
                                        .model(car.getModel())
                                        .variant(car.getVariant())
                                        .fuelType(car.getFuelType())
                                        .price(car.getPrice())
                                        .mileage(car.getMileage())
                                        .maintenanceCost(car.getMaintenanceCost())
                                        .monthlyEmi(round(emi))
                                        .totalMonthlyCost(round(totalMonthlyCost))
                                        .incomeUsagePercent(round(incomeUsagePercent))
                                        .whyRecommended(whyRecommended)
                                        .rank(rank)
                                        .build());

                        rank++;
                }

                return result;
        }

        // ─── Verdict Logic ────────────────────────────────────────────────────────

        private String determineVerdict(StressScoreService.StressResult stressResult,
                        List<CarRecommendationResponse> recommendations) {
                if (recommendations.isEmpty()) {
                        return "DONT_BUY";
                }
                return switch (stressResult.level) {
                        case "SAFE" -> "BUY";
                        case "CAUTION" -> "BUY"; // Can proceed with caution
                        case "RISKY" -> "DONT_BUY";
                        default -> "DONT_BUY";
                };
        }

        // ─── Advice Builder ───────────────────────────────────────────────────────

        private String buildUpgradeAdvice(StressScoreService.StressResult stressResult,
                        double maxCarPrice, List<Car> affordableCars) {
                if (affordableCars.isEmpty()) {
                        return "No cars found within your current budget. Try increasing your down payment, " +
                                        "extending your loan tenure to 7 years, or clearing existing loans first to " +
                                        "increase your safe EMI capacity.";
                }

                return switch (stressResult.level) {
                        case "SAFE" -> String.format(
                                        "Great financial health! You can safely buy up to ₹%.0f. " +
                                                        "Consider fuel efficiency and resale value as tiebreakers. " +
                                                        "You may even stretch your budget slightly without stress.",
                                        maxCarPrice);
                        case "CAUTION" -> String.format(
                                        "You're within range but watch your spending. " +
                                                        "Stick to cars under ₹%.0f. Opt for a 5+ year tenure to reduce monthly EMI. "
                                                        +
                                                        "Build a 6-month emergency fund before buying.",
                                        maxCarPrice * 0.85);
                        case "RISKY" ->
                                "⚠️ We recommend waiting 6–12 months. First close existing loans to free up EMI budget. "
                                                +
                                                "Alternatively, increase your down payment significantly, or target a car under ₹5 Lakhs.";
                        default -> "Review your financial profile and consult a financial advisor.";
                };
        }

        // ─── Why Recommended Builder ──────────────────────────────────────────────

        private String buildWhyRecommended(Car car, int rank, double emi, double incomePercent, double downPayment) {
                String rankLabel = switch (rank) {
                        case 1 -> "Best Match";
                        case 2 -> "Runner Up";
                        case 3 -> "Budget Option";
                        default -> "Alternative";
                };

                return String.format(
                                "%s: %s %s %s fits your budget at ₹%.0f. " +
                                                "Monthly EMI of ₹%.0f uses only %.1f%% of your income. " +
                                                "With ₹%.0f down payment, this is an efficient %s buy.",
                                rankLabel, car.getBrand(), car.getModel(),
                                car.getVariant() != null ? car.getVariant() : "",
                                car.getPrice(), emi, incomePercent, downPayment, car.getFuelType());
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }
}
