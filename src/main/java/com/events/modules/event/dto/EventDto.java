package com.events.modules.event.dto;

import lombok.Builder;

import java.util.UUID;
import java.time.LocalDateTime;

@Builder
public record EventDto(UUID id,
     String name,
     String description,
     String location,
     Double latitude,
     Double longitude,
     LocalDateTime startDate,
     LocalDateTime endDate,
     Integer availableTickets,
     Double price,
     UUID organizerId){}

