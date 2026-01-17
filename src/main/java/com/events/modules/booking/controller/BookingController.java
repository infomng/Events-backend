package com.events.modules.booking.controller;

import com.events.modules.booking.service.IBookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking")
public class BookingController {

//    private final IBookingService bookingService;

    // I will add methods here later
}
