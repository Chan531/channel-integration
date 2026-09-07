package com.server.channel.service.writer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.channel.domain.SupplierCode;
import com.server.channel.domain.SupplierHotel;
import com.server.channel.domain.SupplierRoom;
import com.server.channel.external.dto.GetHotelsResponse;
import com.server.channel.repository.SupplierHotelRepository;
import com.server.channel.repository.SupplierRoomRepository;
import com.server.channel.service.dto.RoomKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelMappingWriter {

    private final SupplierHotelRepository supplierHotelRepository;
    private final SupplierRoomRepository supplierRoomRepository;

    @Transactional
    public void write(SupplierCode code, GetHotelsResponse response) {
        List<GetHotelsResponse.HotelInfo> hotels = response.hotels();

        Map<String, Long> hotelIdByCode = upsertHotels(code, hotels);
        upsertRooms(hotels, hotelIdByCode);
    }

    private Map<String, Long> upsertHotels(SupplierCode code, List<GetHotelsResponse.HotelInfo> hotels) {
        List<String> hotelCodes = hotels.stream()
                .map(GetHotelsResponse.HotelInfo::hotelCode)
                .toList();

        Map<String, SupplierHotel> existingByCode = supplierHotelRepository
                .findByCodeAndHotelCodeIn(code, hotelCodes).stream()
                .collect(Collectors.toMap(SupplierHotel::getHotelCode, Function.identity()));

        List<SupplierHotel> newHotels = hotels.stream()
                .filter(hotel -> !existingByCode.containsKey(hotel.hotelCode()))
                .map(hotel -> SupplierHotel.builder()
                        .code(code)
                        .hotelCode(hotel.hotelCode())
                        .build())
                .toList();

        return Stream.concat(
                        existingByCode.values().stream(),
                        supplierHotelRepository.saveAll(newHotels).stream())
                .collect(Collectors.toMap(SupplierHotel::getHotelCode, SupplierHotel::getId));
    }

    private void upsertRooms(List<GetHotelsResponse.HotelInfo> hotels, Map<String, Long> hotelIdByCode) {
        List<Long> hotelIds = hotelIdByCode.values().stream().distinct().toList();
        List<String> roomCodes = hotels.stream()
                .flatMap(hotel -> hotel.roomInfos().stream())
                .map(GetHotelsResponse.HotelInfo.RoomInfo::roomCode)
                .distinct()
                .toList();

        Set<RoomKey> existingKeys = supplierRoomRepository
                .findByHotelIdInAndRoomCodeIn(hotelIds, roomCodes).stream()
                .map(room -> new RoomKey(room.getHotelId(), room.getRoomCode()))
                .collect(Collectors.toSet());

        List<SupplierRoom> newRooms = hotels.stream()
                .flatMap(hotel -> toUnsavedSupplierRooms(hotel, hotelIdByCode.get(hotel.hotelCode()), existingKeys))
                .toList();

        supplierRoomRepository.saveAll(newRooms);
    }

    private Stream<SupplierRoom> toUnsavedSupplierRooms(
            GetHotelsResponse.HotelInfo hotel,
            Long hotelId,
            Set<RoomKey> existingKeys
    ) {
        return hotel.roomInfos().stream()
                .filter(room -> !existingKeys.contains(new RoomKey(hotelId, room.roomCode())))
                .map(room -> SupplierRoom.builder()
                        .hotelId(hotelId)
                        .roomCode(room.roomCode())
                        .build());
    }
}
