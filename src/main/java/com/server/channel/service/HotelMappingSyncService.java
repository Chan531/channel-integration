package com.server.channel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.server.channel.external.SupplierClient;
import com.server.channel.external.dto.GetHotelsResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelMappingSyncService {

    private final List<SupplierClient> supplierClients;
    private final HotelMappingWriter hotelMappingWriter;

    public void syncHotels() {
        for (SupplierClient client : supplierClients) {
            GetHotelsResponse response = client.getHotels();
            hotelMappingWriter.write(client.getSupplierCode(), response);
        }
    }
}
