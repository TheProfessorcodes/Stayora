package com.stayora.controller;

import com.stayora.dto.BookingDto;
import com.stayora.dto.HotelDto;
import com.stayora.dto.HotelReportDto;
import com.stayora.service.BookingService;
import com.stayora.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createHotel(@RequestBody HotelDto hotelDto) {
        log.info("Attempting to create a hotel with name{}", hotelDto.getName());

        return new ResponseEntity<>(hotelService.createNewHotel(hotelDto), HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) {
        log.info("Attempting to get hotel with id{}", hotelId);
        return new ResponseEntity<>(hotelService.getHotelById(hotelId), HttpStatus.OK);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId,
                                                    @RequestBody HotelDto hotelDto) {
        log.info("Attempting to update hotel with id{}", hotelId);
        HotelDto updatedHotel = hotelService.updateHotelById(hotelId, hotelDto);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
        hotelService.deleteHotelById(hotelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{hotelId}/activate")
    public ResponseEntity<Void> activateHotelById(@PathVariable Long hotelId) {
        hotelService.activateHotelById(hotelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId) {
         return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getAllBookingsByHotelId(@PathVariable Long hotelId,
                                                                  @RequestParam(required=false)LocalDate startDate,
                                                                  @RequestParam(required=false)LocalDate endDate) {
        if(startDate==null) startDate=LocalDate.now().minusMonths(1);
        if(endDate==null) endDate=LocalDate.now();
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }

}