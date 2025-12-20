package com.events.modules.auth.exception;

public class UserNotVerifiedException extends RuntimeException {

    public UserNotVerifiedException(String message) {
        super(message);
    }
}
