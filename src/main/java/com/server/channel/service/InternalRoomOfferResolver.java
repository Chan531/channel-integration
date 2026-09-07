package com.server.channel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.server.channel.domain.SupplierCode;
import com.server.channel.repository.SupplierHotelRepository;
import com.server.channel.repository.SupplierRoomRepository;
import com.server.channel.service.dto.AvailabilityQueryResponse;
import com.server.channel.service.dto.InternalRoomOffer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalRoomOfferResolver {

    private final SupplierHotelRepository supplierHotelRepository;
    private final SupplierRoomRepository supplierRoomRepository;

    public List<InternalRoomOffer> resolve(List<AvailabilityQueryResponse.RoomOffer> offers) {
        Map<HotelKey, Long> hotelIdByKey = resolveHotelIds(offers);
        Map<RoomKey, Long> roomIdByKey = resolveRoomIds(hotelIdByKey, offers);

        return offers.stream()
                .map(offer -> toInternalRoomOffer(offer, hotelIdByKey, roomIdByKey))
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<HotelKey, Long> resolveHotelIds(List<AvailabilityQueryResponse.RoomOffer> offers) {
        Map<SupplierCode, List<String>> hotelCodesBySupplier = offers.stream()
                .collect(Collectors.groupingBy(
                        AvailabilityQueryResponse.RoomOffer::sourceSupplier,
                        Collectors.mapping(AvailabilityQueryResponse.RoomOffer::hotelCode, Collectors.toList())));

        Map<HotelKey, Long> hotelIdByKey = new HashMap<>();
        hotelCodesBySupplier.forEach((code, hotelCodes) ->
                supplierHotelRepository.findByCodeAndHotelCodeIn(code, hotelCodes).forEach(
                        hotel -> hotelIdByKey.put(new HotelKey(code, hotel.getHotelCode()), hotel.getId())));
        return hotelIdByKey;
    }

    private Map<RoomKey, Long> resolveRoomIds(
            Map<HotelKey, Long> hotelIdByKey, List<AvailabilityQueryResponse.RoomOffer> offers
    ) {
        List<Long> hotelIds = hotelIdByKey.values().stream().distinct().toList();
        List<String> roomCodes = offers.stream()
                .map(AvailabilityQueryResponse.RoomOffer::roomCode)
                .distinct()
                .toList();

        Map<RoomKey, Long> roomIdByKey = new HashMap<>();
        supplierRoomRepository.findByHotelIdInAndRoomCodeIn(hotelIds, roomCodes).forEach(
                room -> roomIdByKey.put(new RoomKey(room.getHotelId(), room.getRoomCode()), room.getId()));
        return roomIdByKey;
    }

    private InternalRoomOffer toInternalRoomOffer(
            AvailabilityQueryResponse.RoomOffer offer, Map<HotelKey, Long> hotelIdByKey, Map<RoomKey, Long> roomIdByKey
    ) {
        Long hotelId = hotelIdByKey.get(new HotelKey(offer.sourceSupplier(), offer.hotelCode()));
        if (hotelId == null) {
            log.warn("No internal hotel mapping for supplier {} hotelCode {}", offer.sourceSupplier(), offer.hotelCode());
            return null;
        }

        Long roomId = roomIdByKey.get(new RoomKey(hotelId, offer.roomCode()));
        if (roomId == null) {
            log.warn("No internal room mapping for hotelId {} roomCode {}", hotelId, offer.roomCode());
            return null;
        }

        int availableRooms = offer.dailyInventory().stream()
                .mapToInt(AvailabilityQueryResponse.RoomOffer.DailyInventory::remainingRooms)
                .min()
                .orElse(0);

        return new InternalRoomOffer(
                hotelId,
                offer.hotelName(),
                roomId,
                offer.roomName(),
                offer.maxOccupancy(),
                offer.breakfastIncluded(),
                availableRooms,
                offer.totalPrice(),
                offer.currency(),
                offer.sourceSupplier()
        );
    }

    private record HotelKey(SupplierCode code, String hotelCode) {
    }
}
