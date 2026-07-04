package com.stayora.service;

import com.stayora.dto.BookingDto;
import com.stayora.dto.BookingRequest;
import com.stayora.dto.GuestDto;
import com.stayora.entity.*;
import com.stayora.entity.enums.BookingStatus;
import com.stayora.exception.ResourceNotFoundException;
import com.stayora.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private ModelMapper modelMapper = new ModelMapper();
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Booking request is {}", bookingRequest);

        Hotel hotel=hotelRepository.findById(bookingRequest.getHotelId()).
                orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));

        Room room=roomRepository.findById(bookingRequest.getRoomId()).
                orElseThrow(()-> new ResourceNotFoundException("Room Not Found"));

        List<Inventory> inventoryList=inventoryRepository.
                findAndLockAvailableInventory
                        (room.getId(),bookingRequest.getCheckInDate(),
                                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long dayCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        if(inventoryList.size()!=dayCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the rooms/update the bookedCount of inventories
        for(Inventory inventory:inventoryList){
            inventory.setReservedCount(inventory.getBookedCount()+bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //Create the booking
//        User user =new User();
//        user.setId(1L);//TODO : REMOVE DUMMY USER

        //TODO: CALCULATE DYNAMIC AMOUNT

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        booking=bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }


    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests on request");
        Booking booking =bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking expired");

        }

        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking status is not RESERVED");
        }

        for(GuestDto guestDto:guestDtoList){
            Guest guest=modelMapper.map(guestDto,Guest.class);
            guest.setUser(getCurrentUser());
            guest=guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    public Boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());

    }

    public User getCurrentUser(){
        User user=new User();
        user.setId(1L);
        return user;
    }
}
