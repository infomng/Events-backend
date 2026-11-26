package com.events.modules.event.events;

import java.util.UUID;

/**
 * Publié quand un organisateur annule un événement : les réservations doivent être
 * annulées ou remboursées, et les participants prévenus.
 */
public record EventCancelledEvent(UUID eventId, String eventName) {
}
