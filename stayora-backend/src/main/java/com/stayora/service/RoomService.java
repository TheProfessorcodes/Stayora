package com.stayora.service;

import com.stayora.dto.RoomDto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RoomService {

    RoomDto createNewRoom(Long hotelId,RoomDto roomDto);

    List<RoomDto> getAllRooms(Long hotelId);

    RoomDto getRoomById(Long roomId);

    void deleteById(Long roomId);

}
