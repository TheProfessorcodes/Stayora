package com.stayora.service;

import com.stayora.dto.HotelDto;
import com.stayora.dto.HotelSearchRequest;
import com.stayora.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
