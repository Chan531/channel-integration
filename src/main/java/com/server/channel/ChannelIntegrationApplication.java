package com.server.channel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChannelIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChannelIntegrationApplication.class, args);
    }

}
