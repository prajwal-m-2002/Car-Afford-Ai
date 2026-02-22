package com.caraffordai.controller;

import com.caraffordai.dto.*;
import com.caraffordai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController – Auth and user lookup endpoints.
 *
 * POST /api/auth/register → Create account (name, email, income, password)
 * POST /api/auth/login → Authenticate (email, password) → returns userId
 * GET /api/users/{userId} → Lookup a user by ID
 * POST /api/users/register → Legacy alias (kept for backward compatibility)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ─── Register ────────────────────────────────────────────────────────────

    @PostMapping("/api/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("POST /api/auth/register – email={}", request.getEmail());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Legacy alias — the old wizard used /api/users/register.
     * Keeping it so we don't break anything if called directly.
     */
    @PostMapping("/api/users/register")
    public ResponseEntity<UserResponse> registerLegacy(@Valid @RequestBody UserRegistrationRequest request) {
        return register(request);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @PostMapping("/api/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login – email={}", request.getEmail());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─── Lookup ──────────────────────────────────────────────────────────────

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        log.info("GET /api/users/{}", userId);
        var user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .monthlyIncome(user.getMonthlyIncome())
                .build());
    }
}
