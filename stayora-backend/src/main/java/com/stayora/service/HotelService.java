package com.stayora.service;

import com.stayora.dto.HotelDto;
import com.stayora.dto.HotelInfoDto;
import com.stayora.entity.Hotel;

import java.util.List;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotelById(Long id);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();
//void deactivateHotelById(Long id);
}
