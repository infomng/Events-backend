package com.events.modules.cart.exception;

public class NotEnoughQuantityException extends RuntimeException {
    public NotEnoughQuantityException(String message) {
        super(message);
    }

    public NotEnoughQuantityException() {
        super("Quantity must be greater than 0");
    }
}
