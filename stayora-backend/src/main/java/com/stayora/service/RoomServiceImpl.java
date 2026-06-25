package com.stayora.service;

import com.stayora.dto.RoomDto;
import com.stayora.entity.Hotel;
import com.stayora.entity.Room;
import com.stayora.exception.ResourceNotFoundException;
import com.stayora.repository.HotelRepository;
import com.stayora.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("creating a new Room");

        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found"));
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room=roomRepository.save(room);

        //TODO:CREATE INVENTORY AS SOON AS ROOM IS CREATED AND HOTEL IS ACTIVE
        if(hotel.getActive()){

            inventoryService.initializeRoomForAYear(room);

        }//TODO:DONE

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRooms(Long hotelId) {
        log.info("getting all rooms");
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found"));
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("getting room by id");
        Room room=roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found"));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteById(Long roomId) {
        log.info("deleting room by id");
        Room room=roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found"));

        //TODO:DELETE INVENTORY FUTURE
        inventoryService.deleteFutureInventories(room);

        roomRepository.deleteById(roomId);
    }
}
