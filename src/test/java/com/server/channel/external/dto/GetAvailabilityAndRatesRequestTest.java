package com.server.channel.external.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.server.channel.external.exception.SupplierChunkSizeExceededException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAvailabilityAndRatesRequestTest {

    @Test
    void 호텔코드가_50개면_예외가_발생하지_않는다() {
        List<String> hotelCodes = IntStream.range(0, 50).mapToObj(i -> "A-" + i).toList();

        assertThatCode(() -> new GetAvailabilityAndRatesRequest(
                hotelCodes, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0))
                .doesNotThrowAnyException();
    }

    @Test
    void 호텔코드가_50개를_초과하면_예외가_발생한다() {
        List<String> hotelCodes = IntStream.range(0, 51).mapToObj(i -> "A-" + i).toList();

        assertThatThrownBy(() -> new GetAvailabilityAndRatesRequest(
                hotelCodes, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0))
                .isInstanceOf(SupplierChunkSizeExceededException.class);
    }
}
