package com.server.channel.service;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import com.server.channel.MockSupplierSupport;
import com.server.channel.domain.SupplierCode;
import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.StaySearchResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StaySearchServiceTest {

    @Autowired
    private HotelMappingSyncService hotelMappingSyncService;

    @Autowired
    private StaySearchService staySearchService;

    private WebClient control;

    @BeforeAll
    static void 목_서버를_기동한다() {
        MockSupplierSupport.start();
    }

    @BeforeEach
    void 매핑을_동기화하고_컨트롤_클라이언트를_준비한다() {
        control = WebClient.create("http://localhost:9090");
        hotelMappingSyncService.syncHotels();
    }

    @AfterEach
    void 정상_모드로_되돌린다() {
        control.method(HttpMethod.POST).uri("/control/a/mode?value=normal").retrieve().toBodilessEntity().block();
    }

    @Test
    void 전체_흐름을_거쳐_내부_식별자로_변환된_검색_결과를_반환한다() {
        AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        StaySearchResponse response = staySearchService.searchStays(request);

        assertThat(response.offers()).hasSize(3);
        assertThat(response.offers()).allMatch(offer -> offer.hotelId() != null && offer.roomId() != null);
        assertThat(response.failedSuppliers()).isEmpty();

        boolean hasZeroAvailability = response.offers().stream()
                .anyMatch(offer -> offer.hotelName().equals("Namsan Garden Stay") && offer.availableRooms() == 0);
        assertThat(hasZeroAvailability).isTrue();
    }

    @Test
    void 공급사_장애_시_실패_사실이_결과에_드러난다() {
        control.method(HttpMethod.POST).uri("/control/a/mode?value=error").retrieve().toBodilessEntity().block();

        AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        StaySearchResponse response = staySearchService.searchStays(request);

        assertThat(response.failedSuppliers()).containsExactly(SupplierCode.A);
        assertThat(response.offers()).allMatch(offer -> offer.sourceSupplier() == SupplierCode.B);
    }
}
