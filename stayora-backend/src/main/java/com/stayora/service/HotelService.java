package com.stayora.service;

import com.stayora.dto.HotelDto;
import com.stayora.entity.Hotel;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotelById(Long id);
//void deactivateHotelById(Long id);
}
