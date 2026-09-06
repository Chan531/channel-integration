package com.server.channel.external.dto;

import java.time.LocalDate;
import java.util.List;

public record GetAvailabilityAndRatesResponse(
        List<RoomOffer> offers
) {

    public record RoomOffer(
            String hotelCode,
            String hotelName,
            String roomCode,
            String roomName,
            int maxOccupancy,
            boolean breakfastIncluded,
            long totalPrice,
            String currency,
            List<DailyInventory> dailyInventory
    ) {

        public record DailyInventory(
                LocalDate date,
                int remainingRooms
        ) {
        }
    }
}
