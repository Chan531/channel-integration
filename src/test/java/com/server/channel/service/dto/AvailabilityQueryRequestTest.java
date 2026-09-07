package com.server.channel.service.dto;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvailabilityQueryRequestTest {

    @Test
    void 체크아웃이_체크인보다_늦으면_예외가_발생하지_않는다() {
        assertThatCode(() -> new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4), 2, 0))
                .doesNotThrowAnyException();
    }

    @Test
    void 체크아웃이_체크인과_같으면_예외가_발생한다() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        assertThatThrownBy(() -> new AvailabilityQueryRequest(date, date, 2, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 체크아웃이_체크인보다_빠르면_예외가_발생한다() {
        assertThatThrownBy(() -> new AvailabilityQueryRequest(
                LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 1), 2, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
