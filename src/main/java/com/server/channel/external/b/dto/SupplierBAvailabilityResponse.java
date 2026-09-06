package com.server.channel.external.b.dto;

import java.time.LocalDate;
import java.util.List;

public record SupplierBAvailabilityResponse(
        String resultCode,
        String resultMessage,
        Data data
) {

    public record Data(
            List<RoomRate> items
    ) {

        public record RoomRate(
                String propertyId,
                String propertyName,
                String roomId,
                String roomName,
                int maxOccupancy,
                boolean breakfastIncluded,
                String currency,
                long totalPrice,
                boolean taxIncluded,
                List<Inventory> inventory
        ) {

            public record Inventory(
                    LocalDate date,
                    int remainingRooms
            ) {
            }
        }
    }
}
