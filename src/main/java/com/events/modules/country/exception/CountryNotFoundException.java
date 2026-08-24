package com.events.modules.country.exception;

import java.util.UUID;

public class CountryNotFoundException extends RuntimeException {

    public CountryNotFoundException(UUID id) {
        super("Country with id: " + id + " not found.");
    }

    public CountryNotFoundException(String message) {
        super(message);
    }
}
