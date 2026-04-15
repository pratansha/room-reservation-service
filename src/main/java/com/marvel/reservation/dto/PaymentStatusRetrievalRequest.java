package com.marvel.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentStatusRetrievalRequest {
    private String paymentReference;
}