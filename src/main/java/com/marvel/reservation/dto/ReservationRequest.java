package com.marvel.reservation.dto;

import com.marvel.reservation.enums.PaymentMode;
import com.marvel.reservation.enums.RoomSegment;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Segment is required")
    private RoomSegment segment;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    private String paymentReference;

    // =========================
    // Custom Validations
    // =========================

    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return true;
        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Reservation cannot exceed 30 days")
    public boolean isWithin30Days() {
        if (startDate == null || endDate == null) return true;
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return days > 0 && days <= 30;
    }

    @AssertTrue(message = "Payment reference is required only for credit card payments")
    public boolean isPaymentReferenceValid() {
        if (paymentMode == null) return true;

        if (paymentMode == PaymentMode.CREDIT_CARD) {
            return paymentReference != null && !paymentReference.isBlank();
        }

        return paymentReference == null || paymentReference.isBlank(); // CASH and BANK_TRANSFER don't require reference
    }
}