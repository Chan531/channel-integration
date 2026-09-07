package com.server.channel.external.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class SupplierWebClientConfig {

    @Bean
    public WebClient supplierAWebClient(
            @Value("${supplier.a.base-url}") String baseUrl,
            @Value("${supplier.a.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${supplier.a.response-timeout-ms}") int responseTimeoutMs
    ) {
        return buildWebClient(baseUrl, connectTimeoutMs, responseTimeoutMs);
    }

    @Bean
    public WebClient supplierBWebClient(
            @Value("${supplier.b.base-url}") String baseUrl,
            @Value("${supplier.b.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${supplier.b.response-timeout-ms}") int responseTimeoutMs
    ) {
        return buildWebClient(baseUrl, connectTimeoutMs, responseTimeoutMs);
    }

    private WebClient buildWebClient(String baseUrl, int connectTimeoutMs, int responseTimeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
