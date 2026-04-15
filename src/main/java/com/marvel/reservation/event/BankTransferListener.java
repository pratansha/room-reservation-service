package com.marvel.reservation.event;

import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.repository.ReservationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BankTransferListener {

    private final ReservationRepository repository;

    public BankTransferListener(ReservationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "bank-transfer-payment-update", groupId = "reservation-group")
    public void consume(String message) {

        try {
            // Example: "1401541457 P4145478"
            String[] parts = message.split(" ");

            if (parts.length < 2) {
                throw new RuntimeException("Invalid message format");
            }

            String reservationId = parts[1].trim();

            Reservation reservation = repository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            if (reservation.getStatus() == com.marvel.reservation.enums.ReservationStatus.PENDING_PAYMENT) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                repository.save(reservation);
            }

        } catch (Exception e) {
            // In real-world: send to DLQ
            System.err.println("Error processing Kafka message: " + message);
        }
    }
}