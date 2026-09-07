package com.server.channel.external.a;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import com.server.channel.MockSupplierSupport;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.external.dto.GetAvailabilityAndRatesResponse;
import com.server.channel.external.dto.GetHotelsResponse;
import com.server.channel.external.exception.SupplierUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SupplierAClientTest {

    @Autowired
    private SupplierAClient supplierAClient;

    private WebClient control;

    @BeforeAll
    static void 목_서버를_기동한다() {
        MockSupplierSupport.start();
    }

    @BeforeEach
    void 컨트롤_클라이언트를_준비한다() {
        control = WebClient.create("http://localhost:9090");
    }

    @AfterEach
    void 정상_모드로_되돌린다() {
        setMode("normal");
    }

    @Test
    void 숙소_목록을_정상적으로_가져온다() {
        GetHotelsResponse response = supplierAClient.getHotels();

        assertThat(response.hotels()).hasSize(2);
        assertThat(response.hotels().get(0).hotelCode()).isEqualTo("A-10023");
        assertThat(response.hotels().get(0).roomInfos()).isNotEmpty();
    }

    @Test
    void 재고_요금을_정상적으로_가져오고_날짜별_요금을_합산한다() {
        GetAvailabilityAndRatesRequest request = new GetAvailabilityAndRatesRequest(
                List.of("A-10023"), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        GetAvailabilityAndRatesResponse response = supplierAClient.getAvailabilityAndRates(request);

        // Mock은 요청 파라미터를 무시하고 고정 응답을 주므로, A-10023 항목만 골라서 확인한다.
        GetAvailabilityAndRatesResponse.RoomOffer offer = response.offers().stream()
                .filter(o -> o.hotelCode().equals("A-10023"))
                .findFirst()
                .orElseThrow();

        assertThat(offer.totalPrice()).isEqualTo(429000L);
        assertThat(offer.dailyInventory()).hasSize(3);
    }

    @Test
    void 장애_응답이면_SupplierUnavailableException을_던진다() {
        setMode("error");

        GetAvailabilityAndRatesRequest request = new GetAvailabilityAndRatesRequest(
                List.of("A-10023"), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        assertThatThrownBy(() -> supplierAClient.getAvailabilityAndRates(request))
                .isInstanceOf(SupplierUnavailableException.class);
    }

    private void setMode(String value) {
        control.method(HttpMethod.POST)
                .uri("/control/a/mode?value=" + value)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
