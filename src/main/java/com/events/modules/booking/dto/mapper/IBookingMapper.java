package com.events.modules.booking.dto.mapper;

import com.events.modules.booking.dto.GetBookingDto;
import com.events.modules.booking.dto.GetBookingItemDto;
import com.events.modules.booking.entity.Booking;
import com.events.modules.booking.entity.BookingItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IBookingMapper {

    GetBookingDto toDto(Booking booking);

    GetBookingItemDto toItemDto(BookingItem item);
}
