package com.server.channel.batch;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.server.channel.MockSupplierSupport;
import com.server.channel.repository.SupplierHotelRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class HotelMappingBatchSchedulerTest {

    @Autowired
    private SupplierHotelRepository supplierHotelRepository;

    @BeforeAll
    static void 목_서버를_기동한다() {
        MockSupplierSupport.start();
    }

    @Test
    void 앱_기동과_동시에_스케줄러가_매핑을_채운다() {
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(supplierHotelRepository.findAll()).isNotEmpty());
    }
}
