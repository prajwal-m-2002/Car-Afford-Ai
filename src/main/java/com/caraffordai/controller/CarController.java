package com.caraffordai.controller;

import com.caraffordai.dto.*;
import com.caraffordai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CarController – Car recommendation and report endpoints.
 *
 * GET /api/cars/recommend?userId=1 → Triggers the full recommendation engine
 * GET /api/report/{userId} → Returns the latest saved report
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CarController {

    private final RecommendationService recommendationService;

    /**
     * GET /api/cars/recommend?userId={userId}
     *
     * Key endpoint: Generates a new affordability report for the user.
     * This triggers the full recommendation pipeline:
     * 1. Calculate safe EMI
     * 2. Find max car price
     * 3. Query matching cars
     * 4. Calculate stress score
     * 5. Generate verdict
     * 6. Save report to DB
     * 7. Return top 3 recommendations
     *
     * @param userId The registered user ID
     */
    @GetMapping("/api/cars/recommend")
    public ResponseEntity<AffordabilityReportResponse> recommendCars(
            @RequestParam Long userId) {
        log.info("GET /api/cars/recommend – userId={}", userId);
        AffordabilityReportResponse response = recommendationService.generateReport(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/report/{userId}
     *
     * Returns the most recently generated affordability report for a user.
     *
     * Interview Tip: We separate this from /recommend to allow caching
     * of previously generated reports without re-running the heavy engine.
     */
    @GetMapping("/api/report/{userId}")
    public ResponseEntity<AffordabilityReportResponse> getLatestReport(
            @PathVariable Long userId) {
        log.info("GET /api/report/{}", userId);
        // Re-generates or could fetch last saved report
        AffordabilityReportResponse response = recommendationService.generateReport(userId);
        return ResponseEntity.ok(response);
    }
}
