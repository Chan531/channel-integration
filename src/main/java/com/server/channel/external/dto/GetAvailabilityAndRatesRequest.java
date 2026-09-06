package com.server.channel.external.dto;

import java.time.LocalDate;
import java.util.List;

public record GetAvailabilityAndRatesRequest(
        List<String> hotelCodes,
        LocalDate checkIn,
        LocalDate checkOut,
        int adults,
        int children
) {
}
