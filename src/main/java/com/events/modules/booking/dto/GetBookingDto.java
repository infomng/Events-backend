package com.events.modules.booking.dto;

import com.events.modules.booking.enumeration.BookingStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetBookingDto(
    @Schema(description = "The ID of the booking", example = "1")
    UUID id,

    @Schema(description = "The ID of the event", example = "1")
    UUID eventId,

    @Schema(description = "The ID of the user", example = "1")
    UUID userId,

    @Schema(description = "The list of seat IDs booked", example = "[1, 2, 3]")
    List<UUID> seatIds,

    @Schema(description = "The status of the booking", example = "CREATED")
    BookingStatusEnum status,

    @Schema(description = "The expiration date of the booking")
    LocalDateTime expiresAt
) {}
