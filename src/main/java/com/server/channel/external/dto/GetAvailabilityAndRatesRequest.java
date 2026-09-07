package com.server.channel.external.dto;

import java.time.LocalDate;
import java.util.List;

import com.server.channel.external.exception.SupplierChunkSizeExceededException;

public record GetAvailabilityAndRatesRequest(
        List<String> hotelCodes,
        LocalDate checkIn,
        LocalDate checkOut,
        int adults,
        int children
) {

    public static final int MAX_HOTEL_CODES = 50;

    public GetAvailabilityAndRatesRequest {
        if (hotelCodes.size() > MAX_HOTEL_CODES) {
            throw new SupplierChunkSizeExceededException(
                    "hotelCodes must not exceed " + MAX_HOTEL_CODES + " but was " + hotelCodes.size());
        }
    }
}
