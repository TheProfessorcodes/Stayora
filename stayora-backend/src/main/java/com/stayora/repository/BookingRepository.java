package com.stayora.repository;

import com.stayora.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.lang.ScopedValue;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    Optional<Booking> findByPaymentSessionId(String sessionId);
}
