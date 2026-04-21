package com.events.modules.payment.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(UUID paymentId) {
        super("Payment not found with id: " + paymentId);
    }

    public PaymentNotFoundException(String field, String value) {
        super("Payment not found with " + field + ": " + value);
    }
}
