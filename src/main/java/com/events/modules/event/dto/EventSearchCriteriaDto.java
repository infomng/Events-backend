package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for event search criteria.
 * All parameters are optional and can be combined.
 */
public record EventSearchCriteriaDto(
        // Visibility & state
        Boolean isFeatured,
        Boolean isPublic,
        EventStatusEnum status,
        Boolean isTicketSalesActive,

        // Dates
        LocalDateTime startDateFrom,
        LocalDateTime startDateTo,
        LocalDateTime endDateFrom,
        LocalDateTime endDateTo,
        Boolean upcomingOnly,

        // Location
        UUID countryId,
        String location,
        Double latitude,
        Double longitude,
        Double radiusKm,

        // Categorization
        UUID categoryId,
        UUID organizerId,

        // Ticketing
        Boolean hasSeats,
        Boolean hasAvailableTickets
) {
}
