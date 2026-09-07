package com.server.channel.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.server.channel.domain.SupplierCode;
import com.server.channel.external.SupplierClient;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.external.dto.GetAvailabilityAndRatesResponse;
import com.server.channel.external.exception.SupplierUnavailableException;
import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.AvailabilityQueryResponse;
import com.server.channel.service.dto.HotelCodeChunk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierAvailabilityQueryService {

    private final List<SupplierClient> supplierClients;
    private final SupplierHotelCodeChunker supplierHotelCodeChunker;

    public AvailabilityQueryResponse queryAll(AvailabilityQueryRequest queryRequest) {
        Map<SupplierCode, List<HotelCodeChunk>> chunksBySupplier = supplierHotelCodeChunker.chunksBySupplier();

        List<ChunkTask> tasks = supplierClients.stream()
                .flatMap(client -> chunksBySupplier.getOrDefault(client.getSupplierCode(), List.of()).stream()
                        .map(chunk -> new ChunkTask(client, chunk)))
                .toList();

        List<AvailabilityQueryResponse.RoomOffer> offers = new ArrayList<>();
        Set<SupplierCode> failedSuppliers = new HashSet<>();

        for (ChunkTask task : tasks) {
            SupplierCode code = task.client().getSupplierCode();
            GetAvailabilityAndRatesRequest request = new GetAvailabilityAndRatesRequest(
                    task.chunk().hotelCodes(), queryRequest.checkIn(), queryRequest.checkOut(),
                    queryRequest.adults(), queryRequest.children());
            try {
                task.client().getAvailabilityAndRates(request).offers().stream()
                        .map(offer -> toRoomOffer(code, offer))
                        .forEach(offers::add);
            } catch (SupplierUnavailableException e) {
                log.warn("Failed to query availability for supplier {}: {}", code, e.getMessage());
                failedSuppliers.add(code);
            }
        }

        return new AvailabilityQueryResponse(offers, failedSuppliers);
    }

    private record ChunkTask(SupplierClient client, HotelCodeChunk chunk) {
    }

    private static AvailabilityQueryResponse.RoomOffer toRoomOffer(
            SupplierCode sourceSupplier, GetAvailabilityAndRatesResponse.RoomOffer offer
    ) {
        List<AvailabilityQueryResponse.RoomOffer.DailyInventory> dailyInventory = offer.dailyInventory().stream()
                .map(daily -> new AvailabilityQueryResponse.RoomOffer.DailyInventory(
                        daily.date(), daily.remainingRooms()))
                .toList();

        return new AvailabilityQueryResponse.RoomOffer(
                sourceSupplier,
                offer.hotelCode(),
                offer.hotelName(),
                offer.roomCode(),
                offer.roomName(),
                offer.maxOccupancy(),
                offer.breakfastIncluded(),
                offer.totalPrice(),
                offer.currency(),
                dailyInventory
        );
    }
}
