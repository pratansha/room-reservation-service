package com.marvel.reservation.controller;

import com.marvel.reservation.dto.ReservationRequest;
import com.marvel.reservation.dto.ReservationResponse;
import com.marvel.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @Operation(summary = "Confirm Room Reservation", description = """
            Creates a room reservation based on payment mode:
            - CASH → Immediate confirmation
            - CREDIT_CARD → Calls external payment service
            - BANK_TRANSFER → Pending until Kafka event
            
            Validations:
            - Max reservation: 30 days
            - No overlapping bookings allowed
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request / validation error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "Invalid value 'WRONG' for field 'paymentMode'. Allowed values: [CASH, CREDIT_CARD, BANK_TRANSFER]",
                      "status": 400,
                      "timestamp": "2026-04-15T19:10:00"
                    }
                    """)
            )),
            @ApiResponse(responseCode = "402", description = "Payment failed"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @PostMapping("/confirm")
    public ResponseEntity<ReservationResponse> confirm(@Valid @RequestBody ReservationRequest request) {
        log.info("API HIT → /reservations/confirm");
        ReservationResponse response = service.confirmReservation(request);
        log.info("API SUCCESS → reservationId={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }
}