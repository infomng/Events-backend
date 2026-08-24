package com.events.modules.payment.enumeration;

public enum PaymentGatewayEnum {
    STRIPE,         // Stripe payment gateway
    PAYPAL,         // PayPal gateway
    ORANGE_MONEY,   // Orange Money (Africa)
    MTN_MONEY,      // MTN Mobile Money (Africa)
    WAVE,           // Wave mobile payment
    FLUTTERWAVE,    // Flutterwave (Africa)
    INTERNAL        // Internal/Manual processing
}
