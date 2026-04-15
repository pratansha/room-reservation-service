package com.marvel.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvel.reservation.dto.*;
import com.marvel.reservation.enums.*;
import com.marvel.reservation.exception.PaymentServiceException;
import com.marvel.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationRequest validRequest() {
        ReservationRequest req = new ReservationRequest();
        req.setCustomerName("John");
        req.setRoomNumber("101");
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(2));
        req.setSegment(RoomSegment.MEDIUM);
        req.setPaymentMode(PaymentMode.CASH);
        req.setAmount(5000.0);
        return req;
    }

    @Test
    void shouldReturn200_whenValidRequest() throws Exception {
        ReservationResponse response = new ReservationResponse("P1234567", ReservationStatus.CONFIRMED);
        Mockito.when(service.confirmReservation(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("P1234567"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturn400_whenValidationFails() throws Exception {
        ReservationRequest invalid = new ReservationRequest(); // empty
        mockMvc.perform(post("/reservations/confirm").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalid))).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn503_whenPaymentServiceFails() throws Exception {
        Mockito.when(service.confirmReservation(Mockito.any())).thenThrow(new PaymentServiceException("Service down"));
        mockMvc.perform(post("/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isServiceUnavailable());
    }
}