package com.events.modules.payment.exception;

public class PaymentAlreadyProcessedException extends RuntimeException {
    public PaymentAlreadyProcessedException() {
        super("Payment already processed: ");
    }
}
