package com.server.channel.service.dto;

import com.server.channel.domain.SupplierCode;

public record InternalRoomOffer(
        Long hotelId,
        String hotelName,
        Long roomId,
        String roomName,
        int maxOccupancy,
        boolean breakfastIncluded,
        int availableRooms,
        long totalPrice,
        String currency,
        SupplierCode sourceSupplier
) {
}
