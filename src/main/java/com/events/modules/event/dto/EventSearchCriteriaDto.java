package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for event search criteria.
 * All parameters are optional and can be combined.
 */public record EventSearchCriteriaDto(
        Boolean isFeatured,
        Boolean isPublic,
        EventStatusEnum status,
        Boolean isTicketSalesActive,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDateTo,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDateTo,

        Boolean upcomingOnly,
        UUID countryId,
        String location,
        Double latitude,
        Double longitude,
        Double radiusKm,
        UUID categoryId,
        UUID organizerId,
        Boolean hasSeats,
        Boolean hasAvailableTickets
) {
}
