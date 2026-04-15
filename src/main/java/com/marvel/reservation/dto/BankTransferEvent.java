package com.marvel.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransferEvent {
    private String paymentId;
    private String debtorAccountNumber;
    private Double amountReceived;
    private String transactionDescription;
}