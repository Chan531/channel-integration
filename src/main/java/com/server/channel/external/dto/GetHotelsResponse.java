package com.server.channel.external.dto;

import java.util.List;

public record GetHotelsResponse(
        List<HotelInfo> hotels
) {

    public record HotelInfo(
            String hotelCode,
            String hotelName,
            List<RoomInfo> roomInfos
    ) {

        public record RoomInfo(
                String roomCode,
                String roomName,
                int maxOccupancy
        ) {
        }
    }
}
