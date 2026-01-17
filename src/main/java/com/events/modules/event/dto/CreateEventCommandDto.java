package com.events.modules.event.dto;

import com.events.common.exception.BadRequestException;
import com.events.common.utils.contants.Constants;
import com.events.modules.event.enumeration.EventStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
                                    List<UUID> staffMembers
) { }