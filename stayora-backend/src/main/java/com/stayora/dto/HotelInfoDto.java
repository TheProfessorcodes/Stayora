package com.stayora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDto {

    private HotelDto hotelDto;
    private List<RoomDto> rooms;


//    public HotelInfoDto(HotelDto map, List<RoomDto> rooms) {
//    }
}
