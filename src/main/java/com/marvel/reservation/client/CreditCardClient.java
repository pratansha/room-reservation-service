package com.marvel.reservation.client;

import com.marvel.reservation.dto.PaymentStatusResponse;
import com.marvel.reservation.dto.PaymentStatusRetrievalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "credit-card-client", url = "${credit.card.base-url}")
public interface CreditCardClient {
    @PostMapping("/payment-status")
    PaymentStatusResponse getPaymentStatus(@RequestBody PaymentStatusRetrievalRequest request);
}