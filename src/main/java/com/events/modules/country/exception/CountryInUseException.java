package com.events.modules.country.exception;

import java.util.UUID;

public class CountryInUseException extends RuntimeException {

    public CountryInUseException(UUID id) {
        super("Country with id: " + id + " is currently in use and cannot be deleted.");
    }

    public CountryInUseException(String message) {
        super(message);
    }
}
