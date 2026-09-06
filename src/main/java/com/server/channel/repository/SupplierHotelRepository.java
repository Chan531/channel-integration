package com.server.channel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.channel.domain.SupplierCode;
import com.server.channel.domain.SupplierHotel;

public interface SupplierHotelRepository extends JpaRepository<SupplierHotel, Long> {

    List<SupplierHotel> findByCodeAndHotelCodeIn(SupplierCode code, List<String> hotelCodes);
}
