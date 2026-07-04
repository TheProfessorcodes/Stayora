package com.stayora.dto;

import com.stayora.entity.Guest;
import com.stayora.entity.Hotel;
import com.stayora.entity.Room;
import com.stayora.entity.User;
import com.stayora.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;







}
