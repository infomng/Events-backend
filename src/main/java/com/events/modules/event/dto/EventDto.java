package com.events.modules.event.dto;

import lombok.*;
import java.time.LocalDateTime;

@Builder
public record EventDto(Long id,
     String name,
     String description,
     String location,
     Double latitude,
     Double longitude,
     LocalDateTime startDate,
     LocalDateTime endDate,
     Integer availableTickets,
     Double price,
     Long organizerId){}

