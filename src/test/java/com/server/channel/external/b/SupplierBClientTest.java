package com.server.channel.external.b;

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
class SupplierBClientTest {

    @Autowired
    private SupplierBClient supplierBClient;

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
        GetHotelsResponse response = supplierBClient.getHotels();

        assertThat(response.hotels()).hasSize(1);
        assertThat(response.hotels().get(0).hotelCode()).isEqualTo("B77120");
    }

    @Test
    void 재고_요금을_정상적으로_가져오고_총액을_그대로_사용한다() {
        GetAvailabilityAndRatesRequest request = new GetAvailabilityAndRatesRequest(
                List.of("B77120"), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        GetAvailabilityAndRatesResponse response = supplierBClient.getAvailabilityAndRates(request);

        assertThat(response.offers()).hasSize(1);
        assertThat(response.offers().get(0).totalPrice()).isEqualTo(452000L);
    }

    @Test
    void resultCode가_실패면_HTTP_200이어도_SupplierUnavailableException을_던진다() {
        setMode("error");

        GetAvailabilityAndRatesRequest request = new GetAvailabilityAndRatesRequest(
                List.of("B77120"), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        assertThatThrownBy(() -> supplierBClient.getAvailabilityAndRates(request))
                .isInstanceOf(SupplierUnavailableException.class)
                .hasMessageContaining("E503");
    }

    private void setMode(String value) {
        control.method(HttpMethod.POST)
                .uri("/control/b/mode?value=" + value)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
