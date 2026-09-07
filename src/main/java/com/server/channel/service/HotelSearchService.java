package com.server.channel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.AvailabilityQueryResponse;
import com.server.channel.service.dto.HotelSearchResponse;
import com.server.channel.service.dto.InternalRoomOffer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelSearchService {

    private final SupplierAvailabilityQueryService supplierAvailabilityQueryService;
    private final InternalRoomOfferResolver internalRoomOfferResolver;

    public HotelSearchResponse searchHotels(AvailabilityQueryRequest request) {
        AvailabilityQueryResponse queryResponse = supplierAvailabilityQueryService.queryAll(request);
        List<InternalRoomOffer> offers = internalRoomOfferResolver.resolve(queryResponse.offers());

        return new HotelSearchResponse(offers, queryResponse.failedSuppliers());
    }
}
