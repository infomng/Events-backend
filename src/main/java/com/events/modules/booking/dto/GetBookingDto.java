package com.events.modules.booking.dto;

import com.events.modules.event.enumeration.BookingStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetBookingDto(
        UUID id,
        @Schema(description = "Human readable reference", example = "BK-1A2B3C4D")
        String bookingReference,
        UUID eventId,
        String eventName,
        UUID userId,
        @Schema(description = "Booking status", example = "PENDING")
        BookingStatusEnum status,
        BigDecimal totalAmount,
        @Schema(description = "Payment deadline; tickets are released after it")
        LocalDateTime expiresAt,
        List<GetBookingItemDto> items,
        LocalDateTime createdAt
) {
}
