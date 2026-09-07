package com.server.channel.service.dto;

import java.time.LocalDate;

public record AvailabilityQueryRequest(
        LocalDate checkIn,
        LocalDate checkOut,
        int adults,
        int children
) {

    public AvailabilityQueryRequest {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }
}
