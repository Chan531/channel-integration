package com.server.channel;

import java.util.Map;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.server.channel.mocksupplier.MockSupplierApplication;

public final class MockSupplierSupport {

    private static ConfigurableApplicationContext context;

    private MockSupplierSupport() {
    }

    public static synchronized void start() {
        if (context != null) {
            return;
        }
        context = new SpringApplicationBuilder(MockSupplierApplication.class)
                .properties(Map.of("server.port", "9090"))
                .run();
    }
}
