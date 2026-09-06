package com.server.channel.external.a.dto;

import java.time.LocalDate;
import java.util.List;

public record SupplierAAvailabilityResponse(
        List<RoomRate> items
) {

    public record RoomRate(
            String hotelCode,
            String hotelName,
            String roomTypeCode,
            String roomTypeName,
            int maxOccupancy,
            boolean breakfastIncluded,
            String currency,
            List<DailyRate> dailyRates
    ) {

        public record DailyRate(
                LocalDate date,
                int remainingRooms,
                long nightlyRate,
                long taxAmount
        ) {
        }
    }
}
