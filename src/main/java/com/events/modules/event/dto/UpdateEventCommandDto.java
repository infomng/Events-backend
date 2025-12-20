package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;

import java.time.LocalDateTime;

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
        EventStatusEnum status
) {}

