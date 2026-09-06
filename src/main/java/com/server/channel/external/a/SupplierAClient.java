package com.server.channel.external.a;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import com.server.channel.domain.SupplierCode;
import com.server.channel.external.SupplierClient;
import com.server.channel.external.a.dto.SupplierAAvailabilityResponse;
import com.server.channel.external.a.dto.SupplierAErrorResponse;
import com.server.channel.external.a.dto.SupplierAHotelsResponse;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.external.dto.GetAvailabilityAndRatesResponse;
import com.server.channel.external.dto.GetHotelsResponse;
import com.server.channel.external.exception.SupplierChunkSizeExceededException;
import com.server.channel.external.exception.SupplierUnavailableException;

@Component
public class SupplierAClient implements SupplierClient {

    private static final String TOO_MANY_HOTEL_CODES = "TOO_MANY_HOTEL_CODES";

    private final WebClient webClient;

    public SupplierAClient(@Qualifier("supplierAWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public SupplierCode getSupplierCode() {
        return SupplierCode.A;
    }

    @Override
    public GetHotelsResponse getHotels() {
        SupplierAHotelsResponse response = webClient.get()
                .uri("/a/v1/hotels")
                .retrieve()
                .onStatus(HttpStatusCode::isError, SupplierAClient::toErrorMono)
                .bodyToMono(SupplierAHotelsResponse.class)
                .block();

        List<GetHotelsResponse.HotelInfo> hotels = response.items().stream()
                .map(SupplierAClient::toHotelInfo)
                .toList();

        return new GetHotelsResponse(hotels);
    }

    @Override
    public GetAvailabilityAndRatesResponse getAvailabilityAndRates(GetAvailabilityAndRatesRequest request) {
        SupplierAAvailabilityResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/a/v1/availability")
                        .queryParam("hotelCodes", String.join(",", request.hotelCodes()))
                        .queryParam("checkIn", request.checkIn())
                        .queryParam("checkOut", request.checkOut())
                        .queryParam("adults", request.adults())
                        .queryParam("children", request.children())
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, SupplierAClient::toErrorMono)
                .bodyToMono(SupplierAAvailabilityResponse.class)
                .block();

        List<GetAvailabilityAndRatesResponse.RoomOffer> offers = response.items().stream()
                .map(SupplierAClient::toRoomOffer)
                .toList();

        return new GetAvailabilityAndRatesResponse(offers);
    }

    private static Mono<? extends Throwable> toErrorMono(ClientResponse response) {
        return response.bodyToMono(SupplierAErrorResponse.class)
                .map(SupplierAClient::toException);
    }

    private static RuntimeException toException(SupplierAErrorResponse error) {
        if (TOO_MANY_HOTEL_CODES.equals(error.error())) {
            return new SupplierChunkSizeExceededException(error.message());
        }
        return new SupplierUnavailableException(error.error() + ": " + error.message());
    }

    private static GetHotelsResponse.HotelInfo toHotelInfo(SupplierAHotelsResponse.Hotel hotel) {
        List<GetHotelsResponse.HotelInfo.RoomInfo> roomInfos = hotel.roomTypes().stream()
                .map(SupplierAClient::toRoomInfo)
                .toList();

        return new GetHotelsResponse.HotelInfo(hotel.hotelCode(), hotel.hotelName(), roomInfos);
    }

    private static GetHotelsResponse.HotelInfo.RoomInfo toRoomInfo(SupplierAHotelsResponse.Hotel.RoomType roomType) {
        return new GetHotelsResponse.HotelInfo.RoomInfo(
                roomType.roomTypeCode(), roomType.roomTypeName(), roomType.maxOccupancy());
    }

    private static GetAvailabilityAndRatesResponse.RoomOffer toRoomOffer(SupplierAAvailabilityResponse.RoomRate roomRate) {
        List<GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory> dailyInventory = roomRate.dailyRates().stream()
                .map(SupplierAClient::toDailyInventory)
                .toList();

        long totalPrice = roomRate.dailyRates().stream()
                .mapToLong(dailyRate -> dailyRate.nightlyRate() + dailyRate.taxAmount())
                .sum();

        return new GetAvailabilityAndRatesResponse.RoomOffer(
                roomRate.hotelCode(),
                roomRate.hotelName(),
                roomRate.roomTypeCode(),
                roomRate.roomTypeName(),
                roomRate.maxOccupancy(),
                roomRate.breakfastIncluded(),
                totalPrice,
                roomRate.currency(),
                dailyInventory
        );
    }

    private static GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory toDailyInventory(
            SupplierAAvailabilityResponse.RoomRate.DailyRate dailyRate
    ) {
        return new GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory(
                dailyRate.date(), dailyRate.remainingRooms());
    }
}
