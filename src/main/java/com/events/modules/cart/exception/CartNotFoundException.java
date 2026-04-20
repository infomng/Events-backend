package com.events.modules.cart.exception;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }

    public CartNotFoundException(UUID userId) {
        super("Cart not found for user: " + userId);
    }
}
