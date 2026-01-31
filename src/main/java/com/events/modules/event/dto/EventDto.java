package com.events.modules.event.dto;

import com.events.modules.event.enumeration.EventStatusEnum;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record EventDto(UUID id,
                       String name,
                       String description,
                       Boolean isPublic,
                       Boolean isFreeEntry,
                       Boolean hasInvitationCode,
                       String location,
                       Double latitude,
                       Double longitude,
                       LocalDateTime startDate,
                       LocalDateTime endDate,
                       Integer totalTickets,
                       Integer availableTickets,
                       LocalDateTime ticketSalesStartDate,
                       LocalDateTime ticketSalesEndDate,
                       Double price,
                       Boolean hasSeats,
                       EventStatusEnum status,
                       UUID organizerId,
                       List<PriceCategoryDto> categories
) {

}
