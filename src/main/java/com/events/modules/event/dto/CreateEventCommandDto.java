package com.events.modules.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record CreateEventCommandDto(@NotNull String name,
                                    @NotNull String description,
                                    @NotNull Boolean isPublic,
                                    @NotNull Boolean isFreeEntry,
                                    @NotNull Boolean hasInvitationCode,
                                    @NotNull String isInvitationCodeUnique,
                                    @NotNull String location,
                                    Double latitude,
                                    Double longitude,
                                    @NotNull LocalDateTime startDate,
                                    @NotNull LocalDateTime endDate,
                                    @NotNull Integer totalTickets,
                                    LocalDateTime ticketSalesStartDate,
                                    LocalDateTime ticketSalesEndDate,
                                    Double ticketPrice,
                                    Boolean hasSeats,
                                    List<UUID> staffMembers,
                                    @NotEmpty(message = "At least one price category is required")
                                    List< @Valid PriceCategoryDto> priceCategories,
                                    @NotNull(message = "Category ID is required")
                                    UUID categoryId,
                                    @NotNull(message = "Country ID is required")
                                    UUID countryId
) { }