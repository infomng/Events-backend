package com.events.modules.payment.enumeration;

public enum PaymentStatusEnum {
    PENDING,        // Paiement initié, en attente
    PROCESSING,     // En cours de traitement
    COMPLETED,      // Paiement réussi
    FAILED,         // Paiement échoué
    CANCELLED,      // Paiement annulé
    REFUNDED        // Paiement remboursé
}
