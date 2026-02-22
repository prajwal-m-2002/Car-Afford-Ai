package com.caraffordai.controller;

import com.caraffordai.dto.*;
import com.caraffordai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * FinanceController – Handles the financial profile submission API.
 *
 * This is the second step in the user flow after registration.
 * The user submits their expenses, existing EMIs, and down payment.
 */
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Slf4j
public class FinanceController {

    private final UserService userService;

    /**
     * POST /api/finance/submit
     * Submit or update the user's financial profile.
     *
     * Request body: {
     * "userId": 1,
     * "fixedExpenses": 15000,
     * "existingEmi": 5000,
     * "downPayment": 100000,
     * "preferredTenureYears": 5
     * }
     * Returns: 200 OK with computed safe EMI in message
     */
    @PostMapping("/submit")
    public ResponseEntity<UserResponse> submitFinancialProfile(
            @Valid @RequestBody FinanceSubmitRequest request) {
        log.info("POST /api/finance/submit – userId={}", request.getUserId());
        UserResponse response = userService.submitFinancialProfile(request);
        return ResponseEntity.ok(response);
    }
}
