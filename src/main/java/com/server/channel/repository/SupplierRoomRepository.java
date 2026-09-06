package com.server.channel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.channel.domain.SupplierRoom;

public interface SupplierRoomRepository extends JpaRepository<SupplierRoom, Long> {

    List<SupplierRoom> findByHotelIdInAndRoomCodeIn(List<Long> hotelIds, List<String> roomCodes);
}
