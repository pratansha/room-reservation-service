package com.marvel.reservation.util;

import java.util.Random;

public class ReservationIdGenerator {
    private static final Random RANDOM = new Random();

    public static String generateId() {
        int number = 1000000 + RANDOM.nextInt(9000000); // ensures 7 digits
        return "P" + number;
    }
}