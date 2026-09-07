package com.server.channel.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.server.channel.domain.SupplierCode;
import com.server.channel.domain.SupplierHotel;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.repository.SupplierHotelRepository;
import com.server.channel.service.dto.HotelCodeChunk;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierHotelCodeChunker {

    private final SupplierHotelRepository supplierHotelRepository;

    public Map<SupplierCode, List<HotelCodeChunk>> chunksBySupplier() {
        Map<SupplierCode, List<String>> hotelCodesBySupplier = supplierHotelRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        SupplierHotel::getCode,
                        Collectors.mapping(SupplierHotel::getHotelCode, Collectors.toList())));

        return hotelCodesBySupplier.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> chunk(entry.getValue())));
    }

    private List<HotelCodeChunk> chunk(List<String> hotelCodes) {
        int size = GetAvailabilityAndRatesRequest.MAX_HOTEL_CODES;
        return IntStream.iterate(0, i -> i < hotelCodes.size(), i -> i + size)
                .mapToObj(i -> new HotelCodeChunk(hotelCodes.subList(i, Math.min(i + size, hotelCodes.size()))))
                .toList();
    }
}
