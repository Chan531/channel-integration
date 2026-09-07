package com.server.channel.service;

import java.time.Duration;
import java.time.Instant;
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
import com.server.channel.repository.SupplierHotelRepository;
import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.AvailabilityQueryResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class SupplierAvailabilityQueryServiceTest {

    @Autowired
    private SupplierHotelRepository supplierHotelRepository;

    @Autowired
    private SupplierAvailabilityQueryService supplierAvailabilityQueryService;

    private WebClient control;

    @BeforeAll
    static void 목_서버를_기동한다() {
        MockSupplierSupport.start();
    }

    @BeforeEach
    void 매핑이_채워질_때까지_기다리고_컨트롤_클라이언트를_준비한다() {
        control = WebClient.create("http://localhost:9090");
        // 매핑은 스케줄러(HotelMappingBatchScheduler)가 기동 시 자동으로 채운다.
        // 여기서 또 syncHotels()를 직접 호출하면 스케줄러와 동시에 upsert를 시도하는
        // 레이스가 생길 수 있어(유니크 제약 위반 위험), 채워질 때까지 기다리기만 한다.
        await().atMost(Duration.ofSeconds(10)).until(() -> !supplierHotelRepository.findAll().isEmpty());
    }

    @AfterEach
    void 정상_모드로_되돌린다() {
        setMode("a", "normal");
        setMode("b", "normal");
    }

    @Test
    void 공급사_하나가_실패해도_나머지_공급사_결과는_살아있다() {
        setMode("a", "error");

        AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        AvailabilityQueryResponse response = supplierAvailabilityQueryService.queryAll(request);

        assertThat(response.failedSuppliers()).containsExactly(SupplierCode.A);
        assertThat(response.offers()).isNotEmpty();
        assertThat(response.offers()).allMatch(offer -> offer.sourceSupplier() == SupplierCode.B);
    }

    @Test
    void 여러_공급사_호출은_순차가_아니라_병렬로_실행된다() {
        setMode("a", "no-response");
        setMode("b", "no-response");

        AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0);

        Instant start = Instant.now();
        AvailabilityQueryResponse response = supplierAvailabilityQueryService.queryAll(request);
        Duration elapsed = Duration.between(start, Instant.now());

        // 순차라면 응답 타임아웃(4초)이 공급사 수만큼 곱해져 8초 가까이 걸림. 병렬이면 4초 근방.
        assertThat(elapsed).isLessThan(Duration.ofSeconds(6));
        assertThat(response.failedSuppliers()).containsExactlyInAnyOrder(SupplierCode.A, SupplierCode.B);
    }

    private void setMode(String supplier, String value) {
        control.method(HttpMethod.POST)
                .uri("/control/" + supplier + "/mode?value=" + value)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
