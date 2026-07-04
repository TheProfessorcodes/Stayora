package com.stayora.service;

import com.stayora.dto.BookingDto;
import com.stayora.dto.BookingRequest;
import com.stayora.dto.GuestDto;

import java.util.List;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
