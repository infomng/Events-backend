package com.events.modules.event.exception;

import java.util.UUID;

public class TicketsSoldOutException extends RuntimeException {

    public TicketsSoldOutException(UUID priceCategoryId, int requested) {
        super("Not enough tickets left in price category " + priceCategoryId + " (requested: " + requested + ")");
    }
}
