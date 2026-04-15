package com.marvel.reservation.scheduler;

import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationScheduler {
    private final ReservationRepository repository;

    public ReservationScheduler(ReservationRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cancelUnpaidReservations() {
        List<Reservation> reservations = repository.findByStatus(ReservationStatus.PENDING_PAYMENT);
        LocalDate today = LocalDate.now();
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.CANCELLED) {
                return; // ignore already cancelled
            }
            if (r.getStartDate().minusDays(2).isBefore(today)) {
                r.setStatus(ReservationStatus.CANCELLED);
                repository.save(r);
            }
        }
    }
}