package com.events.modules.payment.exception;

import com.events.modules.payment.enumeration.PaymentStatusEnum;

public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException() {
        super("Invalid payment status.");
    }

    public InvalidPaymentStatusException(PaymentStatusEnum currentStatus, PaymentStatusEnum expectedStatus) {
        super("Invalid payment status. Current: " + currentStatus + ", Expected: " + expectedStatus);
    }
}
