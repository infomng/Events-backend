package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateEventCommandDto(
        String name,
        String description,
        String location,
        Double latitude,
        Double longitude,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime ticketSalesStartDate,
        LocalDateTime ticketSalesEndDate,
        Integer totalTickets,
        Double ticketPrice,
        EventStatusEnum status,
        @Valid List<PriceCategoryDto> categories
) {}

