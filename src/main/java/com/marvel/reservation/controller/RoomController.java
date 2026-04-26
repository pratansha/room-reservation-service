package com.marvel.reservation.controller;

import com.marvel.reservation.entity.Room;
import com.marvel.reservation.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService service;

    @GetMapping
    public List<Room> all() {
        return service.getAll();
    }

    @GetMapping("/available")
    public List<Room> available() {
        return service.getAvailable();
    }

    @PostMapping("/reserve/{id}")
    public Room reserve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.reserve(id, jwt.getClaim("preferred_username"));
    }
}