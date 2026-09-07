package com.server.channel.service.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.server.channel.domain.SupplierCode;

public record AvailabilityQueryResponse(
        List<RoomOffer> offers,
        Set<SupplierCode> failedSuppliers
) {

    public record RoomOffer(
            SupplierCode sourceSupplier,
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
