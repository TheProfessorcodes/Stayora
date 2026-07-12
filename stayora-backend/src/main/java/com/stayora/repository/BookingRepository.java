package com.stayora.repository;

import com.stayora.entity.Booking;
import com.stayora.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.lang.ScopedValue;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
