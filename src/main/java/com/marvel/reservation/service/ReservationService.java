package com.marvel.reservation.service;

import com.marvel.reservation.client.CreditCardClient;
import com.marvel.reservation.dto.PaymentStatusResponse;
import com.marvel.reservation.dto.PaymentStatusRetrievalRequest;
import com.marvel.reservation.dto.ReservationRequest;
import com.marvel.reservation.dto.ReservationResponse;
import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.PaymentMode;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.exception.BadRequestException;
import com.marvel.reservation.exception.PaymentFailedException;
import com.marvel.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository repository;
    private final CreditCardClient creditCardClient;

    public ReservationService(ReservationRepository repository,
                              CreditCardClient creditCardClient) {
        this.repository = repository;
        this.creditCardClient = creditCardClient;
    }

    public ReservationResponse confirmReservation(ReservationRequest request) {

        log.info("Received reservation request for customer: {}", request.getCustomerName());

        // 1. Validate input
        validateRequest(request);

        // 2. Idempotency check
        if (request.getPaymentReference() != null && repository.existsByPaymentReference(request.getPaymentReference())) {
            throw new BadRequestException("Duplicate payment reference");
        }

        // 3. Room availability check (IMPORTANT BUSINESS LOGIC)
        boolean isRoomOccupied = repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        request.getRoomNumber(),
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT),
                        request.getEndDate(),
                        request.getStartDate()
                );

        if (isRoomOccupied) {
            throw new BadRequestException("Room is already occupied for the selected dates");
        }

        // 4. Create reservation
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);
        reservation.setCreatedAt(LocalDateTime.now());

        // 5. Payment handling
        switch (request.getPaymentMode()) {
            case CASH:
                reservation.setStatus(ReservationStatus.CONFIRMED);
                break;

            case CREDIT_CARD:
                handleCreditCardPayment(reservation, request);
                break;

            case BANK_TRANSFER:
                reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
                break;

            default:
                throw new BadRequestException("Invalid payment mode");
        }

        // 6. Save
        Reservation saved = repository.save(reservation);
        log.info("Reservation created with id: {} and status: {}", saved.getId(), saved.getStatus());
        return new ReservationResponse(saved.getId(), saved.getStatus());
    }

    // =========================
    // Payment Handling
    // =========================
    private void handleCreditCardPayment(Reservation reservation, ReservationRequest request) {
        PaymentStatusResponse response;
        try {
            var paymentRequest = PaymentStatusRetrievalRequest.builder().paymentReference(request.getPaymentReference()).build();
            response = creditCardClient.getPaymentStatus(paymentRequest);
        } catch (Exception e) {
            log.error("Credit card service failed", e);
            throw new PaymentFailedException("Credit card service unavailable");
        }
        if (response == null || response.getStatus() == null) {
            throw new PaymentFailedException("Invalid response from payment service");
        }
        if ("CONFIRMED".equalsIgnoreCase(response.getStatus())) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
        } else {
            throw new PaymentFailedException("Credit card payment rejected");
        }
    }

    // =========================
    // Validation Layer
    // =========================
    private void validateRequest(ReservationRequest request) {


        // Payment reference validation
        if (request.getPaymentMode() != PaymentMode.CASH && (request.getPaymentReference() == null || request.getPaymentReference().isBlank())) {
            throw new BadRequestException("Payment reference is required");
        }
        validateDates(request);
    }

    private void validateDates(ReservationRequest request) {

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        long days = ChronoUnit.DAYS.between(
                request.getStartDate(),
                request.getEndDate()
        );

        if (days <= 0 || days > 30) {
            throw new BadRequestException("Reservation must be between 1 and 30 days");
        }
    }
}