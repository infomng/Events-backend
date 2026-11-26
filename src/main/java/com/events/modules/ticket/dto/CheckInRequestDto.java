package com.events.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckInRequestDto(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotBlank(message = "QR code content is required")
        String qrPayload
) {
}
