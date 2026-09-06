package com.server.channel.external;

import com.server.channel.domain.SupplierCode;
import com.server.channel.external.dto.GetAvailabilityAndRatesRequest;
import com.server.channel.external.dto.GetAvailabilityAndRatesResponse;
import com.server.channel.external.dto.GetHotelsResponse;

public interface SupplierClient {

    SupplierCode getSupplierCode();

    GetHotelsResponse getHotels();

    GetAvailabilityAndRatesResponse getAvailabilityAndRates(GetAvailabilityAndRatesRequest request);
}
