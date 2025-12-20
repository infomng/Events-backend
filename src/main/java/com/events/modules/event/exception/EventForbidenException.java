package com.events.modules.event.exception;

import com.events.modules.auth.exception.ForbidenException;

import java.util.UUID;

public class EventForbidenException extends ForbidenException {

    public EventForbidenException(UUID id) {
        super("You cannot access the event with id: " + id );
    }
}
