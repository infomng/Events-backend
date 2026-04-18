package com.events.modules.event.dto;

import com.events.modules.category.dto.CategoryDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.event.enumeration.EventStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GetEventDto(
                          @NotNull
                          UUID id,
                          @NotBlank
                          String name,
                          @NotBlank
                          String description,
                          @NotNull
                          Boolean isPublic,
                          @NotNull
                          Boolean isFeatured,
                          @NotNull
                          Boolean isFreeEntry,
                          @NotNull
                          Boolean hasInvitationCode,
                          @NotBlank
                          String location,
                          Double latitude,
                          Double longitude,
                          @NotNull
                          LocalDateTime startDate,
                          @NotNull
                          LocalDateTime endDate,
                          @NotNull
                          Integer totalTickets,
                          @NotNull
                          Integer availableTickets,
                          @NotNull
                          LocalDateTime ticketSalesStartDate,
                          @NotNull
                          LocalDateTime ticketSalesEndDate,
                          @NotNull
                          Boolean hasSeats,
                          @NotNull
                          EventStatusEnum status,
                          @NotNull
                          UUID organizerId,
                          @NotNull
                          CategoryDto category,
                          @NotNull
                          CountryDto country,
                          @NotEmpty
                          List<PriceCategoryDto> priceCategories,
                          @NotEmpty
                          List<ImageDto> images
) {

}
