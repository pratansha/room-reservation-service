package com.marvel.reservation.scheduler;

import com.marvel.reservation.entity.Room;
import com.marvel.reservation.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final RoomRepository roomRepository;

    @PostConstruct
    public void loadData() {
        log.info("Loading initial room data...");

        roomRepository.save(new Room(null, "Room A", true));
        roomRepository.save(new Room(null, "Room B", true));
        roomRepository.save(new Room(null, "Room C", false));
        roomRepository.save(new Room(null, "Room D", true));

        log.info("Rooms loaded successfully");
    }
}