package com.events.modules.payment.enumeration;

public enum PaymentMethodEnum {
    CREDIT_CARD,    // Carte de crédit
    DEBIT_CARD,     // Carte de débit
    PAYPAL,         // PayPal
    STRIPE,         // Stripe
    MOBILE_MONEY,   // Mobile money (Orange Money, MTN, etc.)
    BANK_TRANSFER,  // Virement bancaire
    CASH            // Paiement en espèces (pour événements physiques)
}
