package com.server.channel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.AvailabilityQueryResponse;
import com.server.channel.service.dto.InternalRoomOffer;
import com.server.channel.service.dto.StaySearchResponse;
import com.server.channel.service.resolver.InternalRoomOfferResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaySearchService {

    private final SupplierAvailabilityQueryService supplierAvailabilityQueryService;
    private final InternalRoomOfferResolver internalRoomOfferResolver;

    public StaySearchResponse searchStays(AvailabilityQueryRequest request) {
        AvailabilityQueryResponse queryResponse = supplierAvailabilityQueryService.queryAll(request);
        List<InternalRoomOffer> offers = internalRoomOfferResolver.resolve(queryResponse.offers());

        return new StaySearchResponse(offers, queryResponse.failedSuppliers());
    }
}
