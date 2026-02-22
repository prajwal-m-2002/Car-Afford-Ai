package com.caraffordai.controller;

import com.caraffordai.dto.*;
import com.caraffordai.service.EmiCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * EmiController – Standalone EMI calculation endpoint.
 *
 * This endpoint can be used independently (e.g., landing page calculator)
 * without requiring full user registration.
 *
 * Interview Tip: Design APIs so they can be used both standalone
 * and as part of a larger flow. This increases API reusability.
 */
@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
@Slf4j
public class EmiController {

    private final EmiCalculationService emiCalculationService;

    /**
     * POST /api/emi/calculate
     * Calculate monthly EMI, total payable, and interest.
     *
     * Request body: {
     * "principal": 500000,
     * "annualInterestRate": 8.5,
     * "tenureYears": 5
     * }
     * Returns: Full EMI breakdown
     */
    @PostMapping("/calculate")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(
            @Valid @RequestBody EmiCalculationRequest request) {
        log.info("POST /api/emi/calculate – principal={}, rate={}, tenure={}yr",
                request.getPrincipal(), request.getAnnualInterestRate(), request.getTenureYears());
        EmiCalculationResponse response = emiCalculationService.calculateEmi(request);
        return ResponseEntity.ok(response);
    }
}
