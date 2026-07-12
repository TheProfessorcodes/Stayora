package com.stayora.service;

import com.stayora.dto.BookingDto;
import com.stayora.dto.BookingRequest;
import com.stayora.dto.GuestDto;
import com.stayora.dto.HotelReportDto;
import com.stayora.entity.*;
import com.stayora.entity.enums.BookingStatus;
import com.stayora.exception.ResourceNotFoundException;
import com.stayora.exception.UnAuthorisedException;
import com.stayora.repository.*;
import com.stayora.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.stayora.util.AppUtils.getCurrentUser;

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
    private final CheckOutService checkOutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

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
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        //Create the booking
//        User user =new User();
//        user.setId(1L);//TODO : REMOVE DUMMY USER

        //TODO: CALCULATE DYNAMIC AMOUNT
        BigDecimal priceForOneRoom=pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice=priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking=bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }


    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests on request");
        Booking booking =bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong");
        }
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

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong");
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking expired");

        }
        String sessionUrl=checkOutService.getCheckOutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");
        booking.setBookingStatus((BookingStatus.PAYMENT_PENDING) );
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if ("checkout.session.completed".equals(event.getType())) {

            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (session == null) {
                return;
            }

            String sessionId = session.getId();

            Booking booking = bookingRepository
                    .findByPaymentSessionId(sessionId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Booking not found for session ID: " + sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );

            inventoryRepository.confirmBooking(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );

            log.info(
                    "Successfully confirmed the booking for Booking ID: {}",
                    booking.getId()
            );

        } else {

            log.warn(
                    "Unhandled event type: {}",
                    event.getType()
            );
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking =bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong");
        }
        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Booking status is not confirmed");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );

        inventoryRepository.cancelBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );

        //Refund
        try{
            Session session=Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams= RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong");
        }

        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel=hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));
        User user=getCurrentUser();
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("you are not the owner");

        List<Booking> bookings=bookingRepository.findByHotel(hotel);


        return bookings.stream()
                .map((element)->modelMapper.map(element,BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel=hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));
        User user=getCurrentUser();
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("you are not the owner");
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Booking> bookings=bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);
        Long totalConfirmedBookings=bookings
                .stream()
                .filter(booking->booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueOfConfirmedBookings=bookings.stream()
                .filter(booking->booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue=totalConfirmedBookings ==0 ?BigDecimal.ZERO:
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    public Boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());

    }


}
