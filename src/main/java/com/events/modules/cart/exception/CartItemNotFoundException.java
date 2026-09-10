package com.events.modules.cart.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }

    public CartItemNotFoundException(UUID cartItemId) {
        super("Cart item not found with id: " + cartItemId);
    }
}
