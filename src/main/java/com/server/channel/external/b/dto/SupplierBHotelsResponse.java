package com.server.channel.external.b.dto;

import java.util.List;

public record SupplierBHotelsResponse(
        String resultCode,
        String resultMessage,
        Data data
) {

    public record Data(
            List<Property> items
    ) {

        public record Property(
                String propertyId,
                String propertyName,
                List<Room> rooms
        ) {

            public record Room(
                    String roomId,
                    String roomName,
                    int maxOccupancy
            ) {
            }
        }
    }
}
