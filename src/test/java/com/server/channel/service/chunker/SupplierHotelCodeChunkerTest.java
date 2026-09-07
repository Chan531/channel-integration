package com.server.channel.service.chunker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.server.channel.domain.SupplierCode;
import com.server.channel.domain.SupplierHotel;
import com.server.channel.repository.SupplierHotelRepository;
import com.server.channel.service.dto.HotelCodeChunk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupplierHotelCodeChunkerTest {

    @Test
    void 정확히_50개면_청크_1개로_나뉜다() {
        SupplierHotelRepository repository = mock(SupplierHotelRepository.class);
        when(repository.findAll()).thenReturn(hotelsOf(SupplierCode.A, 50));

        Map<SupplierCode, List<HotelCodeChunk>> result = new SupplierHotelCodeChunker(repository).chunksBySupplier();

        assertThat(result.get(SupplierCode.A)).hasSize(1);
        assertThat(result.get(SupplierCode.A).get(0).hotelCodes()).hasSize(50);
    }

    @Test
    void 숙소가_51개면_청크_2개로_나뉜다() {
        SupplierHotelRepository repository = mock(SupplierHotelRepository.class);
        when(repository.findAll()).thenReturn(hotelsOf(SupplierCode.A, 51));

        Map<SupplierCode, List<HotelCodeChunk>> result = new SupplierHotelCodeChunker(repository).chunksBySupplier();

        List<HotelCodeChunk> chunks = result.get(SupplierCode.A);
        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).hotelCodes()).hasSize(50);
        assertThat(chunks.get(1).hotelCodes()).hasSize(1);
    }

    @Test
    void 공급사별로_독립적으로_그룹핑된다() {
        SupplierHotelRepository repository = mock(SupplierHotelRepository.class);
        List<SupplierHotel> hotels = new ArrayList<>();
        hotels.addAll(hotelsOf(SupplierCode.A, 3));
        hotels.addAll(hotelsOf(SupplierCode.B, 2));
        when(repository.findAll()).thenReturn(hotels);

        Map<SupplierCode, List<HotelCodeChunk>> result = new SupplierHotelCodeChunker(repository).chunksBySupplier();

        assertThat(result.get(SupplierCode.A).get(0).hotelCodes()).hasSize(3);
        assertThat(result.get(SupplierCode.B).get(0).hotelCodes()).hasSize(2);
    }

    private static List<SupplierHotel> hotelsOf(SupplierCode code, int count) {
        List<SupplierHotel> hotels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hotels.add(SupplierHotel.builder().code(code).hotelCode(code + "-" + i).build());
        }
        return hotels;
    }
}
