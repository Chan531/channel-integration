package com.server.channel.external.b;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.server.channel.domain.SupplierCode;
import com.server.channel.external.SupplierClient;
import com.server.channel.external.b.dto.SupplierBAvailabilityResponse;
import com.server.channel.external.b.dto.SupplierBHotelsResponse;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.external.dto.GetAvailabilityAndRatesResponse;
import com.server.channel.external.dto.GetHotelsResponse;

@Component
public class SupplierBClient implements SupplierClient {

    private static final String SUCCESS_RESULT_CODE = "0000";

    private final WebClient webClient;

    public SupplierBClient(@Qualifier("supplierBWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public SupplierCode getSupplierCode() {
        return SupplierCode.B;
    }

    @Override
    public GetHotelsResponse getHotels() {
        SupplierBHotelsResponse response = webClient.get()
                .uri("/b/api/properties")
                .retrieve()
                .bodyToMono(SupplierBHotelsResponse.class)
                .block();

        validateSuccess(response.resultCode(), response.resultMessage());

        List<GetHotelsResponse.HotelInfo> hotels = response.data().items().stream()
                .map(SupplierBClient::toHotelInfo)
                .toList();

        return new GetHotelsResponse(hotels);
    }

    @Override
    public GetAvailabilityAndRatesResponse getAvailabilityAndRates(GetAvailabilityAndRatesRequest request) {
        SupplierBAvailabilityResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/b/api/search")
                        .queryParam("propertyIds", String.join(",", request.hotelCodes()))
                        .queryParam("checkIn", request.checkIn())
                        .queryParam("checkOut", request.checkOut())
                        .queryParam("adults", request.adults())
                        .queryParam("children", request.children())
                        .build())
                .retrieve()
                .bodyToMono(SupplierBAvailabilityResponse.class)
                .block();

        validateSuccess(response.resultCode(), response.resultMessage());

        List<GetAvailabilityAndRatesResponse.RoomOffer> offers = response.data().items().stream()
                .map(SupplierBClient::toRoomOffer)
                .toList();

        return new GetAvailabilityAndRatesResponse(offers);
    }

    private static void validateSuccess(String resultCode, String resultMessage) {
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw new IllegalStateException(
                    "Supplier B request failed: resultCode=" + resultCode + ", resultMessage=" + resultMessage);
        }
    }

    private static GetHotelsResponse.HotelInfo toHotelInfo(SupplierBHotelsResponse.Data.Property property) {
        List<GetHotelsResponse.HotelInfo.RoomInfo> roomInfos = property.rooms().stream()
                .map(SupplierBClient::toRoomInfo)
                .toList();

        return new GetHotelsResponse.HotelInfo(property.propertyId(), property.propertyName(), roomInfos);
    }

    private static GetHotelsResponse.HotelInfo.RoomInfo toRoomInfo(SupplierBHotelsResponse.Data.Property.Room room) {
        return new GetHotelsResponse.HotelInfo.RoomInfo(room.roomId(), room.roomName(), room.maxOccupancy());
    }

    private static GetAvailabilityAndRatesResponse.RoomOffer toRoomOffer(
            SupplierBAvailabilityResponse.Data.RoomRate roomRate
    ) {
        List<GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory> dailyInventory = roomRate.inventory().stream()
                .map(SupplierBClient::toDailyInventory)
                .toList();

        return new GetAvailabilityAndRatesResponse.RoomOffer(
                roomRate.propertyId(),
                roomRate.propertyName(),
                roomRate.roomId(),
                roomRate.roomName(),
                roomRate.maxOccupancy(),
                roomRate.breakfastIncluded(),
                roomRate.totalPrice(),
                roomRate.currency(),
                dailyInventory
        );
    }

    private static GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory toDailyInventory(
            SupplierBAvailabilityResponse.Data.RoomRate.Inventory inventory
    ) {
        return new GetAvailabilityAndRatesResponse.RoomOffer.DailyInventory(
                inventory.date(), inventory.remainingRooms());
    }
}
