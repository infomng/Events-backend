package com.events.modules.event.dto;

import com.events.common.exception.BadRequestException;
import com.events.common.utils.contants.Constants;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventCommandDto(@NotNull String name,
                                    @NotNull String description,
                                    @NotNull String location,
                                    @NotNull LocalDateTime startDate,
                                    @NotNull LocalDateTime endDate,
                                    @NotNull Integer totalTickets,
                                    @NotNull Boolean isFree,
                                    @NotNull Boolean isPublic,
                                    @NotNull Boolean isFreeEntry,
                                    Double latitude,
                                    Double longitude,
                                    LocalDateTime ticketSalesStartDate,
                                    LocalDateTime ticketSalesEndDate,
                                    Double ticketPrice,
                                    Boolean hasSeats) {

    public CreateEventCommandDto {

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(Constants.INVALID_DATE);
        }

        if (ticketSalesStartDate() != null
                && ticketSalesEndDate() != null
                && ticketSalesStartDate().isAfter(ticketSalesEndDate())) {
            throw new BadRequestException(Constants.INVALID_START_DATE);
        }

        if (!isFree() && (ticketPrice() == null || ticketPrice() <= 0)) {
            throw new BadRequestException(Constants.PRICE_MUST_BE_POSITIVE);
        }

        if (isFree() && (ticketPrice() != null)) {
            throw new BadRequestException(Constants.PRICE_MUST_BE_NULL_FOR_FREE_EVENT);
        }
    }
}