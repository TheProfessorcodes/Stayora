package com.stayora.service;

import com.stayora.dto.HotelDto;
import com.stayora.dto.HotelInfoDto;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private ModelMapper modelMapper = new ModelMapper();
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new Hotel with a name{}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);
        hotel=hotelRepository.save(hotel);
        log.info("Hotel with name {} has been created with id{}",
                hotelDto.getName(),hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting a Hotel with id {}",id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with id "+id+" not found"));
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating a Hotel with id {}",id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with id "+id+" not found"));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel=hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with id "+id+" not found"));

        for(Room room:hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
//        return true;
    }

    @Override
    @Transactional
    public void activateHotelById(Long id) {
        log.info("Activating a Hotel with id {}",id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with id "+id+" not found"));
        hotel.setActive(true);
        //TODO : CREATE INVENTORY FOR ALL THE ROOMS FOR THIS HOTEL
        //Assuming only doing it once activating for the first time
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }//DONE
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with id "+hotelId+" not found"));
        List<RoomDto> rooms=hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element,RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }
}
