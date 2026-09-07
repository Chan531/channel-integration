package com.server.channel.external.config;

import java.time.Duration;
import java.util.function.Predicate;

import reactor.util.retry.Retry;

public final class SupplierRetrySpec {

    private SupplierRetrySpec() {
    }

    public static Retry exponentialBackoff(Predicate<Throwable> isRetryable) {
        return Retry.backoff(2, Duration.ofMillis(200))
                .filter(isRetryable)
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
