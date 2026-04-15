package com.marvel.reservation.dto;

import com.marvel.reservation.enums.PaymentMode;
import com.marvel.reservation.enums.RoomSegment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Room segment is required")
    private RoomSegment segment;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String paymentReference;

    private Double amount;

    // Custom validation for date logic
    @AssertTrue(message = "End date must be after start date and within 30 days")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return true;

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return !endDate.isBefore(startDate) && days > 0 && days <= 30;
    }

    @AssertTrue(message = "Payment reference is required for non-cash payments")
    public boolean isPaymentReferenceValid() {
        if (paymentMode == null) return true;

        if (paymentMode != PaymentMode.CASH) {
            return paymentReference != null && !paymentReference.isBlank();
        }
        return true;
    }
}