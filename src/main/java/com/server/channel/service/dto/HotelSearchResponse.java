package com.server.channel.service.dto;

import java.util.List;
import java.util.Set;

import com.server.channel.domain.SupplierCode;

public record HotelSearchResponse(
        List<InternalRoomOffer> offers,
        Set<SupplierCode> failedSuppliers
) {
}
