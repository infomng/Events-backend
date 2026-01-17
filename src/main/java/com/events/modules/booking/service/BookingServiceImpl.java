package com.events.modules.booking.service;
//
//import com.events.common.exception.BadRequestException;
//import com.events.modules.booking.dto.GetBookingDto;
//import com.events.modules.booking.dto.mapper.IBookingMapper;
//import com.events.modules.booking.entity.Booking;
//import com.events.modules.booking.repository.IBookingRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class BookingServiceImpl implements IBookingService {
//
//    private final IBookingRepository bookingRepository;
//
//    private final IBookingMapper bookingMapper;
//
//
//    @Override
//    public GetBookingDto findById(UUID bookingId) {
//        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BadRequestException("Booking not found"));
//        return bookingMapper.toGetBookingDto(booking);
//    }
//}
