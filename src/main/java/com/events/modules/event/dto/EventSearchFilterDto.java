package com.events.modules.event.dto;

import java.time.LocalDateTime;

public record EventSearchFilterDto(
        String name,
        String location,
        Boolean isPublic,
        LocalDateTime startDateFrom,
        LocalDateTime startDateTo,
        LocalDateTime endDateFrom,
        LocalDateTime endDateTo,
        Double priceMin,
        Double priceMax,
        Boolean isFreeEntry,
        Boolean hasSeats,
        String status
) {
}
