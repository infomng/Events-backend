package com.events.modules.event.dto;

import com.events.modules.event.enumeration.SeatTypeEnum;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record GenerateSeatDto(
        @NotBlank
        String seatNumber,
        @NotBlank
        BigDecimal price,
        String section,
        String rowNumber,
        SeatTypeEnum seatType) {
}
