package com.events.modules.event.exception;

import java.util.UUID;

public class PriceCategoryNotFoundException extends RuntimeException {

    public PriceCategoryNotFoundException(UUID id){
        super("Price category not found for event with id" + id );
    }
}
