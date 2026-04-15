package com.marvel.reservation.exception;

import com.marvel.reservation.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // =========================
    // Resource Not Found
    // =========================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // =========================
    // Payment Not Found (Feign 404)
    // =========================
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // =========================
    // Bad Request
    // =========================
    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // =========================
    // Payment Failure
    // =========================
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex) {
        log.warn("Payment failed: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.PAYMENT_REQUIRED);
    }

    // =========================
    // External Service Error
    // =========================
    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorResponse> handlePaymentService(PaymentServiceException ex) {
        log.error("Payment service error: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    // =========================
    // Validation Errors (@Valid)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", message);
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    // =========================
    // Invalid JSON / Enum Parsing
    // =========================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayload(HttpMessageNotReadableException ex) {

        String message = "Invalid request payload";

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {

            String fieldName = ife.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(ife.getValue());

            String allowedValues = ife.getTargetType().isEnum()
                    ? Arrays.toString(ife.getTargetType().getEnumConstants())
                    : "";

            message = String.format(
                    "Invalid value '%s' for field '%s'. Allowed values: %s",
                    invalidValue,
                    fieldName,
                    allowedValues
            );
        }

        log.warn("Invalid payload: {}", message);
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    // =========================
    // Generic Fallback
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================
    // Common Response Builder
    // =========================
    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        ErrorResponse error = ErrorResponse.builder()
                .error(message) //
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}