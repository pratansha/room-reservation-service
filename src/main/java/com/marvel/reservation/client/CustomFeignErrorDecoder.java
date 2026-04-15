package com.marvel.reservation.client;

import com.marvel.reservation.exception.PaymentNotFoundException;
import com.marvel.reservation.exception.PaymentServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = extractMessage(response);
        return switch (response.status()) {
            case 400 -> new IllegalArgumentException(errorMessage);
            case 404 -> new PaymentNotFoundException(errorMessage);
            case 500 -> new PaymentServiceException("Payment service internal error");
            default -> new PaymentServiceException("Unexpected error from payment service");
        };
    }

    private String extractMessage(Response response) {
        try (InputStream body = response.body().asInputStream()) {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Unable to read error response";
        }
    }
}