package com.events.modules.cart.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String message) {
        super(message);
    }

    public InvalidQuantityException() {
        super("Quantity must be greater than 0");
    }
}
