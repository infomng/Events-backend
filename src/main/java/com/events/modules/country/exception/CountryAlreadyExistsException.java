package com.events.modules.country.exception;

public class CountryAlreadyExistsException extends RuntimeException {

    public CountryAlreadyExistsException(String name) {
        super("Country with name '" + name + "' already exists.");
    }
}
