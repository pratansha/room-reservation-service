package com.marvel.reservation.service;

import com.marvel.reservation.entity.Reservation;
import com.marvel.reservation.entity.Room;
import com.marvel.reservation.repository.ReservationRepository;
import com.marvel.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public List<Room> getAll() {
        log.info("Fetching all rooms");
        return roomRepository.findAll();
    }

    public List<Room> getAvailable() {
        log.info("Fetching available rooms");
        return roomRepository.findByAvailableTrue();
    }

    public Room reserve(Long roomId, String username) {
        log.info("Reserving room {} for user {}", roomId, username);

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isAvailable()) {
            log.warn("Room {} already booked", roomId);
            throw new RuntimeException("Room already booked");
        }

        // Update room
        room.setAvailable(false);
        roomRepository.save(room);

        // Save reservation
        Reservation reservation = new Reservation();
        reservation.setId(String.valueOf(roomId));
        reservation.setCustomerName(username);
        reservationRepository.save(reservation);
        return room;
    }
}