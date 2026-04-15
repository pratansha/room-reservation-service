package com.marvel.reservation.entity;

import com.marvel.reservation.enums.PaymentMode;
import com.marvel.reservation.enums.ReservationStatus;
import com.marvel.reservation.enums.RoomSegment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String customerName;

    private String roomNumber;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private RoomSegment segment;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(unique = true)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Double amount;

    private LocalDateTime createdAt;
}