package com.server.channel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.server.channel.domain.SupplierCode;
import com.server.channel.external.SupplierClient;
import com.server.channel.external.dto.GetHotelsResponse;
import com.server.channel.external.exception.SupplierUnavailableException;
import com.server.channel.service.writer.HotelMappingWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelMappingSyncService {

    private final List<SupplierClient> supplierClients;
    private final HotelMappingWriter hotelMappingWriter;

    public void syncHotels() {
        for (SupplierClient client : supplierClients) {
            SupplierCode code = client.getSupplierCode();
            try {
                GetHotelsResponse response = client.getHotels();
                hotelMappingWriter.write(code, response);
            } catch (SupplierUnavailableException e) {
                log.warn("Failed to sync hotels for supplier {}: {}", code, e.getMessage());
            }
        }
    }
}
