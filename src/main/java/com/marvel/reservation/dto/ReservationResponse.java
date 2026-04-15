package com.marvel.reservation.dto;

import com.marvel.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationResponse {
    private String reservationId;
    private ReservationStatus status;
}