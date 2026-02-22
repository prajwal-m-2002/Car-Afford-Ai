package com.caraffordai.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;

/**
 * GlobalExceptionHandler – Centralized error handling for all REST APIs.
 *
 * Interview Tip: @RestControllerAdvice is a combination of @ControllerAdvice
 * + @ResponseBody.
 * It intercepts exceptions thrown from any controller and converts them into
 * standardized JSON error responses – the user never sees a raw stack trace.
 *
 * This pattern is also called "Exception Shield" because it protects
 * the API from leaking internal implementation details.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─── Error Response Builder ──────────────────────────────────────────────

    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    // ─── Handle 404: Resource Not Found ─────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        Map<String, Object> body = buildErrorBody(
                HttpStatus.NOT_FOUND, "Resource Not Found",
                ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ─── Handle 409: Duplicate Resource ─────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        log.error("Duplicate resource: {}", ex.getMessage());
        Map<String, Object> body = buildErrorBody(
                HttpStatus.CONFLICT, "Duplicate Resource",
                ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ─── Handle 400: Business Validation ────────────────────────────────────

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(
            BusinessValidationException ex, WebRequest request) {
        log.error("Business validation failed: {}", ex.getMessage());
        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST, "Business Validation Error",
                ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ─── Handle 400: Jakarta Bean Validation Errors ──────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields have invalid values. Check 'fieldErrors' for details.",
                request.getDescription(false).replace("uri=", ""));
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ─── Handle 500: Generic / Unexpected ────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        Map<String, Object> body = buildErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
