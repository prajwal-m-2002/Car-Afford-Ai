package com.caraffordai.service;

import com.caraffordai.dto.*;
import com.caraffordai.entity.*;
import com.caraffordai.exception.*;
import com.caraffordai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService – Handles auth (register/login) and financial profile management.
 *
 * Interview Tip: We use BCryptPasswordEncoder for secure password hashing.
 * BCrypt is slow by design (work factor = 10) — ideal for password storage
 * because it makes brute-force attacks computationally expensive.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

        private final UserRepository userRepository;
        private final ExpenseRepository expenseRepository;

        /** BCrypt encoder — strength 10 = ~100ms per hash, good balance */
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        /** EMI Safety Ratio from configuration (default: 0.40 = 40%) */
        @Value("${carafford.emi.safety-ratio:0.40}")
        private double emiSafetyRatio;

        // ─── Registration ────────────────────────────────────────────────────────

        /**
         * Register a new user with hashed password.
         * Prevents duplicate email registrations.
         */
        public UserResponse registerUser(UserRegistrationRequest request) {
                log.info("Registering new user: {}", request.getEmail());

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateResourceException(
                                        String.format("Email '%s' is already registered. Please login instead.",
                                                        request.getEmail()));
                }

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .monthlyIncome(request.getMonthlyIncome())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .build();

                User saved = userRepository.save(user);
                log.info("User registered successfully – ID: {}", saved.getId());

                return toResponse(saved, "Welcome to CarAfford AI, " + saved.getName() + "! 🎉");
        }

        // ─── Login ───────────────────────────────────────────────────────────────

        /**
         * Authenticate user with email + password.
         * Uses BCrypt.matches() — constant-time comparison prevents timing attacks.
         */
        public UserResponse login(LoginRequest request) {
                log.info("Login attempt for: {}", request.getEmail());

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new BusinessValidationException(
                                                "Invalid email or password. Please check your credentials."));

                // Null check for legacy users without password
                if (user.getPasswordHash() == null ||
                                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        throw new BusinessValidationException(
                                        "Invalid email or password. Please check your credentials.");
                }

                log.info("Login successful for user ID: {}", user.getId());
                return toResponse(user, "Welcome back, " + user.getName() + "! 👋");
        }

        // ─── Finance Profile ─────────────────────────────────────────────────────

        /**
         * Save or update the user's financial expense profile.
         */
        public UserResponse submitFinancialProfile(FinanceSubmitRequest request) {
                log.info("Submitting financial profile for user ID: {}", request.getUserId());

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

                double totalObligations = request.getFixedExpenses() + request.getExistingEmi();
                if (totalObligations >= user.getMonthlyIncome()) {
                        throw new BusinessValidationException(
                                        String.format("Total monthly obligations (₹%.0f) cannot exceed income (₹%.0f).",
                                                        totalObligations, user.getMonthlyIncome()));
                }

                Expense expense = expenseRepository.findByUserId(request.getUserId())
                                .orElse(new Expense());

                expense.setUser(user);
                expense.setFixedExpenses(request.getFixedExpenses());
                expense.setExistingEmi(request.getExistingEmi());
                expense.setDownPayment(request.getDownPayment());
                expense.setPreferredTenureYears(request.getPreferredTenureYears());
                expenseRepository.save(expense);

                double safeEmi = (user.getMonthlyIncome() - request.getFixedExpenses()) * emiSafetyRatio;

                return toResponse(user, String.format(
                                "Financial profile saved. Safe EMI ≈ ₹%.0f/month.", safeEmi));
        }

        // ─── Helpers ─────────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public User getUserById(Long userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        }

        @Transactional(readOnly = true)
        public Expense getExpenseByUserId(Long userId) {
                return expenseRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Financial profile", "userId",
                                                userId));
        }

        private UserResponse toResponse(User user, String message) {
                return UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .monthlyIncome(user.getMonthlyIncome())
                                .message(message)
                                .build();
        }
}
