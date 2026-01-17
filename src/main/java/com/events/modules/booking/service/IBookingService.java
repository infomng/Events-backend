package com.events.modules.booking.service;

import com.events.modules.booking.dto.GetBookingDto;
import com.events.modules.booking.entity.Booking;

import java.util.UUID;

public interface IBookingService {
    GetBookingDto findById(UUID bookingId);


}
