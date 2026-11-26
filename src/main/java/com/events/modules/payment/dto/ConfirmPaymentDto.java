package com.events.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Demande de synchronisation d'un paiement avec la passerelle.
 * Le statut est toujours lu chez Stripe : le client ne peut fournir
 * ni identifiant de transaction ni réponse de passerelle.
 */
public record ConfirmPaymentDto(
        @NotBlank(message = "Payment reference is required")
        String paymentReference
) {
}
