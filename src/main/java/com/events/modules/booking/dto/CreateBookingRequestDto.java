package com.events.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequestDto(
    @Schema(description = "The ID of the event to book", example = "1")
    @NotNull(message = "Event ID cannot be null")
    UUID eventId,

    @Schema(description = "The list of seat IDs to book", example = "[1, 2, 3]")
    @NotNull(message = "Seat IDs cannot be null")
    List<Long> seatIds
) {}
