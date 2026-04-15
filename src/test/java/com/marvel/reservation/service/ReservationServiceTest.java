package com.marvel.reservation.service;

import com.marvel.reservation.client.CreditCardClient;
import com.marvel.reservation.dto.*;
import com.marvel.reservation.enums.*;
import com.marvel.reservation.exception.*;
import com.marvel.reservation.repository.ReservationRepository;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private CreditCardClient creditCardClient;

    @InjectMocks
    private ReservationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private ReservationRequest buildRequest(PaymentMode mode) {
        ReservationRequest req = new ReservationRequest();
        req.setCustomerName("John");
        req.setRoomNumber("101");
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));
        req.setSegment(RoomSegment.MEDIUM);
        req.setPaymentMode(mode);
        req.setAmount(5000.0);
        req.setPaymentReference("REF123");
        return req;
    }

    // =========================
    // SUCCESS CASES
    // =========================

    @Test
    void shouldConfirmReservation_forCash() {
        ReservationRequest request = buildRequest(PaymentMode.CASH);

        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(false);

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationResponse response = service.confirmReservation(request);

        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void shouldConfirmReservation_forCreditCard_whenConfirmed() {
        ReservationRequest request = buildRequest(PaymentMode.CREDIT_CARD);

        PaymentStatusResponse paymentResponse = new PaymentStatusResponse();
        paymentResponse.setStatus("CONFIRMED");

        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(false);

        when(creditCardClient.getPaymentStatus(any())).thenReturn(paymentResponse);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationResponse response = service.confirmReservation(request);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void shouldMarkPending_forBankTransfer() {
        ReservationRequest request = buildRequest(PaymentMode.BANK_TRANSFER);

        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = service.confirmReservation(request);
        assertEquals(ReservationStatus.PENDING_PAYMENT, response.getStatus());
    }

    // =========================
    // NEGATIVE CASES
    // =========================

    @Test
    void shouldThrowBadRequest_whenDuplicatePaymentReference() {
        ReservationRequest request = buildRequest(PaymentMode.CREDIT_CARD);
        when(repository.existsByPaymentReference(any())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.confirmReservation(request));
    }

    @Test
    void shouldThrowBadRequest_whenRoomOccupied() {
        ReservationRequest request = buildRequest(PaymentMode.CASH);
        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.confirmReservation(request));
    }

    @Test
    void shouldThrowPaymentFailed_whenCreditCardRejected() {
        ReservationRequest request = buildRequest(PaymentMode.CREDIT_CARD);
        PaymentStatusResponse paymentResponse = new PaymentStatusResponse();
        paymentResponse.setStatus("REJECTED");
        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(false);
        when(creditCardClient.getPaymentStatus(any())).thenReturn(paymentResponse);
        assertThrows(PaymentFailedException.class, () -> service.confirmReservation(request));
    }

    @Test
    void shouldThrowPaymentServiceException_whenServiceUnavailable() {
        ReservationRequest request = buildRequest(PaymentMode.CREDIT_CARD);
        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(false);
        when(creditCardClient.getPaymentStatus(any())).thenThrow(mock(RetryableException.class));
        assertThrows(PaymentServiceException.class, () -> service.confirmReservation(request));
    }

    @Test
    void shouldThrowPaymentServiceException_whenInvalidResponse() {
        ReservationRequest request = buildRequest(PaymentMode.CREDIT_CARD);
        when(repository.existsByPaymentReference(any())).thenReturn(false);
        when(repository.existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any())).thenReturn(false);
        when(creditCardClient.getPaymentStatus(any())).thenReturn(null);
        assertThrows(PaymentServiceException.class, () -> service.confirmReservation(request));
    }
}