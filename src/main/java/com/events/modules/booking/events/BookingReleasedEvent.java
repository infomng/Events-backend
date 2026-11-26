package com.events.modules.booking.events;

import java.util.UUID;

/**
 * Publié quand une réservation non payée est expirée ou annulée : ses places sont libérées
 * et tout paiement encore en cours doit être annulé.
 */
public record BookingReleasedEvent(UUID bookingId) {
}
