package com.events.modules.booking.dto;

import java.util.UUID;

/**
 * Coordonnées de l'acheteur d'une réservation, pour lui envoyer une notification.
 */
public record BookingNoticeDto(
        UUID bookingId,
        String bookingReference,
        String eventName,
        String userEmail,
        String userFullName
) {
}
