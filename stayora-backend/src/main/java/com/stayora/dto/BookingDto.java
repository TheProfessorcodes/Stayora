package com.stayora.dto;


import com.stayora.entity.enums.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
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
    private BigDecimal amount;


}
