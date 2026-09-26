package com.events.modules.cart.exception;

public class TooMuchQuantityException extends RuntimeException {
    public TooMuchQuantityException() {
        super("Requested quantity exceeds available tickets for this price category.");
    }
}
