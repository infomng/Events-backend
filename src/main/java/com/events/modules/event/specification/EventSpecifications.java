package com.events.modules.event.specification;

import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.EventStatusEnum;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Specifications for Event entity search.
 * Each specification is pure and returns null if the criterion is absent.
 */
public class EventSpecifications {

    private EventSpecifications() {
        // Utility class
    }

    /**
     * Filter events by PUBLISHED status.
     */
    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), EventStatusEnum.PUBLISHED);
    }

    /**
     * Filter events by public visibility.
     *
     * @param isPublic true for public events, false for private, null to ignore
     */
    public static Specification<Event> isPublic(Boolean isPublic) {
        if (isPublic == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isPublic"), isPublic);
    }

    public static Specification<Event> isFeatured(Boolean isFeatured) {
        if (isFeatured == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isFeatured"), isFeatured);
    }

    /**
     * Filter events by status.
     *
     * @param status the event status
     */
    public static Specification<Event> hasStatus(EventStatusEnum status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filter events by ticket sales active status.
     *
     * @param isActive true for active sales, false for inactive, null to ignore
     */
    public static Specification<Event> isTicketSalesActive(Boolean isActive) {
        if (isActive == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isTicketSalesActive"), isActive);
    }

    /**
     * Filter events starting after a given date.
     *
     * @param from the minimum start date
     */
    public static Specification<Event> startsAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    /**
     * Filter events starting before a given date.
     *
     * @param to the maximum start date
     */
    public static Specification<Event> startsBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), to);
    }

    /**
     * Filter events ending after a given date.
     *
     * @param from the minimum end date
     */
    public static Specification<Event> endsAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), from);
    }

    /**
     * Filter events ending before a given date.
     *
     * @param to the maximum end date
     */
    public static Specification<Event> endsBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), to);
    }

    /**
     * Filter events by country.
     *
     * @param countryId the country UUID
     */
    public static Specification<Event> hasCountry(UUID countryId) {
        if (countryId == null) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Object, Object> country = root.join("country", JoinType.INNER);
            return cb.equal(country.get("id"), countryId);
        };
    }

    /**
     * Filter events by location (exact match, case-insensitive).
     *
     * @param location the location string
     */
    public static Specification<Event> hasLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("location")), location.toLowerCase());
    }

    /**
     * Filter events by category.
     *
     * @param categoryId the category UUID
     */
    public static Specification<Event> hasCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Object, Object> category = root.join("category", JoinType.INNER);
            return cb.equal(category.get("id"), categoryId);
        };
    }

    /**
     * Filter events by organizer.
     *
     * @param organizerId the organizer UUID
     */
    public static Specification<Event> hasOrganizer(UUID organizerId) {
        if (organizerId == null) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Object, Object> organizer = root.join("organizer", JoinType.INNER);
            return cb.equal(organizer.get("id"), organizerId);
        };
    }

    /**
     * Filter events with seats.
     *
     * @param hasSeats true for events with seats, false without, null to ignore
     */
    public static Specification<Event> hasSeats(Boolean hasSeats) {
        if (hasSeats == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("hasSeats"), hasSeats);
    }

    /**
     * Filter events with available tickets.
     */
    public static Specification<Event> hasAvailableTickets() {
        return (root, query, cb) -> cb.greaterThan(root.get("availableTickets"), 0);
    }

    /**
     * Filter events within a geographic bounding box.
     * Uses simple bounding box calculation (not PostGIS).
     *
     * @param latitude  center latitude
     * @param longitude center longitude
     * @param radiusKm  radius in kilometers
     */
    public static Specification<Event> withinGeoBoundingBox(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null || radiusKm == null) {
            return null;
        }

        // Simple bounding box calculation (approximate)
        // 1 degree latitude ~ 111 km
        // 1 degree longitude varies by latitude, but we use a simple approximation
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLon = longitude - lonDelta;
        double maxLon = longitude + lonDelta;

        return (root, query, cb) -> {
            Predicate latPredicate = cb.between(root.get("latitude"), minLat, maxLat);
            Predicate lonPredicate = cb.between(root.get("longitude"), minLon, maxLon);
            return cb.and(latPredicate, lonPredicate);
        };
    }
}
