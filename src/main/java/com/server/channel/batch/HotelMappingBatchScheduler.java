package com.server.channel.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.server.channel.service.HotelMappingSyncService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HotelMappingBatchScheduler {

    private final HotelMappingSyncService hotelMappingSyncService;

    @Scheduled(initialDelay = 0, fixedDelayString = "${hotel-mapping.sync.fixed-delay-ms}")
    public void syncHotels() {
        hotelMappingSyncService.syncHotels();
    }
}
