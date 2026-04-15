package com.marvel.reservation.event;

import com.marvel.reservation.dto.BankTransferEvent;
import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.exception.ReservationNotFoundException;
import com.marvel.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BankTransferListener {
    private final ReservationRepository repository;

    @Value("${custom.kafka.topic.bank-transfer}")
    private String topic;

    public BankTransferListener(ReservationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${custom.kafka.topic.bank-transfer}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(BankTransferEvent event) {
        log.info("Kafka consumer started for topic: {}", topic);
        log.info("Received event: {}", event);
        try {
            // Extract reservationId from transactionDescription
            String[] parts = event.getTransactionDescription().split(" ");

            if (parts.length < 2) {
                log.error("Invalid transactionDescription format");
                return;
            }

            String reservationId = parts[1];
            Reservation reservation = repository.findById(reservationId).orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                log.info("Given Reservation already cancelled");
                return;
            }

            // Optional: validate amount
            if (event.getAmountReceived() >= reservation.getAmount()) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                repository.save(reservation);
                log.info("Reservation confirmed for id: {}", reservationId);
            } else {
                log.warn("Partial payment received for reservation: {}", reservationId);
            }
        } catch (Exception e) {
            log.error("Error processing Kafka event", e);
        }
    }
}