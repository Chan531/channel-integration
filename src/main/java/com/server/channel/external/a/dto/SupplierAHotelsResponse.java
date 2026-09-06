package com.server.channel.external.a.dto;

import java.util.List;

public record SupplierAHotelsResponse(
        List<Hotel> items
) {

    public record Hotel(
            String hotelCode,
            String hotelName,
            List<RoomType> roomTypes
    ) {

        public record RoomType(
                String roomTypeCode,
                String roomTypeName,
                int maxOccupancy
        ) {
        }
    }
}
