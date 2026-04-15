package com.marvel.reservation.dto;

import lombok.Data;

@Data
public class PaymentStatusResponse {
    private String lastUpdateDate;
    private String status; // CONFIRMED / REJECTED
}