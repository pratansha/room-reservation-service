package com.marvel.reservation.repository;

import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

    List<Reservation> findByStatus(ReservationStatus status);
    boolean existsByPaymentReference(String paymentReference);

    boolean existsByRoomNumberAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String roomNumber,
            List<ReservationStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}