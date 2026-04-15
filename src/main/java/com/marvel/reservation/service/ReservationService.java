package com.marvel.reservation.service;

import com.marvel.reservation.client.CreditCardClient;
import com.marvel.reservation.dto.PaymentStatusResponse;
import com.marvel.reservation.dto.PaymentStatusRetrievalRequest;
import com.marvel.reservation.dto.ReservationRequest;
import com.marvel.reservation.dto.ReservationResponse;
import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.exception.BadRequestException;
import com.marvel.reservation.exception.PaymentFailedException;
import com.marvel.reservation.exception.PaymentServiceException;
import com.marvel.reservation.repository.ReservationRepository;
import com.marvel.reservation.util.ReservationIdGenerator;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        log.info("Creating reservation | customer={}, room={}, start={}, end={}, paymentMode={}",
                request.getCustomerName(),
                request.getRoomNumber(),
                request.getStartDate(),
                request.getEndDate(),
                request.getPaymentMode());

        // Idempotency
        if (request.getPaymentReference() != null && repository.existsByPaymentReference(request.getPaymentReference())) {
            log.warn("Duplicate payment reference detected: {}", request.getPaymentReference());
            throw new BadRequestException("Duplicate payment reference");
        }

        // Room availability
        boolean isOccupied = repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getRoomNumber(),
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT),
                request.getEndDate(),
                request.getStartDate()
        );

        if (isOccupied) {
            log.warn("Room already occupied | room={}, start={}, end={}", request.getRoomNumber(), request.getStartDate(), request.getEndDate());
            throw new BadRequestException("Room is already occupied for selected dates");
        }

        // Create entity
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);

        reservation.setId(ReservationIdGenerator.generateId());
        reservation.setCreatedAt(LocalDateTime.now());

        // Payment handling
        switch (request.getPaymentMode()) {
            case CASH -> {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                log.info("Cash payment → auto confirmed");
            }
            case CREDIT_CARD -> handleCreditCardPayment(reservation, request);

            case BANK_TRANSFER -> {
                reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
                log.info("Bank transfer → pending payment");
            }
            default -> throw new BadRequestException("Invalid payment mode");
        }

        Reservation saved = repository.save(reservation);
        log.info("Reservation created successfully | id={}, status={}", saved.getId(), saved.getStatus());
        return new ReservationResponse(saved.getId(), saved.getStatus());
    }

    // =========================
    // Payment Handling
    // =========================
    private void handleCreditCardPayment(Reservation reservation, ReservationRequest request) {

        log.info("Processing credit card payment | reference={}", request.getPaymentReference());

        var paymentRequest = PaymentStatusRetrievalRequest.builder().paymentReference(request.getPaymentReference()).build();
        PaymentStatusResponse response;
        try {
            response = creditCardClient.getPaymentStatus(paymentRequest);
        } catch (RetryableException ex) {
            log.error("Payment service unreachable", ex);
            throw new PaymentServiceException("Payment service unavailable");
        }
        if (response == null || response.getStatus() == null) {
            throw new PaymentServiceException("Invalid payment response from credit card service");
        }

        if ("CONFIRMED".equalsIgnoreCase(response.getStatus())) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            log.info("Payment confirmed");
        } else {
            log.warn("Payment rejected | reference={}", request.getPaymentReference());
            throw new PaymentFailedException("Credit card payment rejected");
        }
    }
}