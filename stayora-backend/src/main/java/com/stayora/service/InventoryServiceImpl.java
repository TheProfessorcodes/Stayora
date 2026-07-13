package com.stayora.service;

import com.stayora.dto.HotelPriceDto;
import com.stayora.dto.HotelSearchRequest;
import com.stayora.dto.InventoryDto;
import com.stayora.dto.UpdateInventoryRequestDto;
import com.stayora.entity.Inventory;
import com.stayora.entity.Room;
import com.stayora.entity.User;
import com.stayora.exception.ResourceNotFoundException;
import com.stayora.repository.HotelMinPriceRepository;
import com.stayora.repository.InventoryRepository;
import com.stayora.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.stayora.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    @Override
    public void initializeRoomForAYear(Room room) {

        LocalDate today=LocalDate.now();
        LocalDate endDate=today.plusYears(1);
        for(; !today.isAfter(endDate); today=today.plusDays(1)){

            Inventory inventory= Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .city(room.getHotel().getCity())
                    .date((today))
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);

        }

    }

    @Override
    public void deleteAllInventories(Room room) {

        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching Hotels for inventory");
        Pageable pageable= PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount=ChronoUnit.DAYS.between(
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate()
        )+1;
        Page<HotelPriceDto> hotelPage=hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomCount(),
                dateCount,pageable);

        return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("");
       Room room=roomRepository.findById(roomId)
               .orElseThrow(()->new ResourceNotFoundException("Room not found"));
       User user=getCurrentUser();
       if(!user.equals(room.getHotel().getOwner())){throw new AccessDeniedException("Access denied");}
       return inventoryRepository.findByRoomOrderByDate(room).stream()
               .map((element)->modelMapper.map(element,InventoryDto.class))
               .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        Room room=roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found"));
        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){throw new AccessDeniedException("Access denied");}

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate(),updateInventoryRequestDto.getClosed(),updateInventoryRequestDto.getSurgeFactor());

    }
}
