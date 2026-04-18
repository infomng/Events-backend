package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection DTO for event search results.
 * Contains only essential information without loading full Event entity.
 */
public record EventSearchViewDto(
        UUID id,
        String name,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        Double latitude,
        Double longitude,
        Boolean hasSeats,
        Integer availableTickets,
        Integer totalTickets,
        EventStatusEnum status,
        Boolean isPublic,
        Boolean isTicketSalesActive,
        CategorySummaryDto category,
        CountrySummaryDto country
) {
    /**
     * Summary DTO for Category information in search results.
     */
    public record CategorySummaryDto(
            UUID id,
            String name
    ) {
    }

    /**
     * Summary DTO for Country information in search results.
     */
    public record CountrySummaryDto(
            UUID id,
            String name,
            String code
    ) {
    }
}
