package com.server.channel.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.channel.service.StaySearchService;
import com.server.channel.service.dto.AvailabilityQueryRequest;
import com.server.channel.service.dto.StaySearchResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stays")
@RequiredArgsConstructor
public class StaySearchController {

    private final StaySearchService staySearchService;

    @GetMapping("/search")
    public StaySearchResponse search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int adults,
            @RequestParam(defaultValue = "0") int children
    ) {
        AvailabilityQueryRequest request = new AvailabilityQueryRequest(checkIn, checkOut, adults, children);
        return staySearchService.searchStays(request);
    }
}
