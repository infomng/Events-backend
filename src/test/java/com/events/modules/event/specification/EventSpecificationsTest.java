package com.events.modules.event.specification;

import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.EventStatusEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSpecificationsTest {

    @Mock
    private Root<Event> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate predicate;

    @Test
    void isPublished_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.isPublished();

        assertNotNull(spec);
    }

    @Test
    void isPublic_WithTrue_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.isPublic(true);

        assertNotNull(spec);
    }

    @Test
    void isPublic_WithFalse_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.isPublic(false);

        assertNotNull(spec);
    }

    @Test
    void isPublic_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.isPublic(null);

        assertNull(spec);
    }

    @Test
    void hasStatus_WithStatus_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.hasStatus(EventStatusEnum.PUBLISHED);

        assertNotNull(spec);
    }

    @Test
    void hasStatus_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasStatus(null);

        assertNull(spec);
    }

    @Test
    void isTicketSalesActive_WithTrue_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.isTicketSalesActive(true);

        assertNotNull(spec);
    }

    @Test
    void isTicketSalesActive_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.isTicketSalesActive(null);

        assertNull(spec);
    }

    @Test
    void startsAfter_WithDate_ShouldReturnSpecification() {
        LocalDateTime date = LocalDateTime.now();
        Specification<Event> spec = EventSpecifications.startsAfter(date);

        assertNotNull(spec);
    }

    @Test
    void startsAfter_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.startsAfter(null);

        assertNull(spec);
    }

    @Test
    void startsBefore_WithDate_ShouldReturnSpecification() {
        LocalDateTime date = LocalDateTime.now();
        Specification<Event> spec = EventSpecifications.startsBefore(date);

        assertNotNull(spec);
    }

    @Test
    void startsBefore_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.startsBefore(null);

        assertNull(spec);
    }

    @Test
    void endsAfter_WithDate_ShouldReturnSpecification() {
        LocalDateTime date = LocalDateTime.now();
        Specification<Event> spec = EventSpecifications.endsAfter(date);

        assertNotNull(spec);
    }

    @Test
    void endsAfter_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.endsAfter(null);

        assertNull(spec);
    }

    @Test
    void endsBefore_WithDate_ShouldReturnSpecification() {
        LocalDateTime date = LocalDateTime.now();
        Specification<Event> spec = EventSpecifications.endsBefore(date);

        assertNotNull(spec);
    }

    @Test
    void endsBefore_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.endsBefore(null);

        assertNull(spec);
    }

    @Test
    void hasCountry_WithUUID_ShouldReturnSpecification() {
        UUID countryId = UUID.randomUUID();
        Specification<Event> spec = EventSpecifications.hasCountry(countryId);

        assertNotNull(spec);
    }

    @Test
    void hasCountry_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasCountry(null);

        assertNull(spec);
    }

    @Test
    void hasLocation_WithValidString_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.hasLocation("Paris");

        assertNotNull(spec);
    }

    @Test
    void hasLocation_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasLocation(null);

        assertNull(spec);
    }

    @Test
    void hasLocation_WithBlankString_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasLocation("   ");

        assertNull(spec);
    }

    @Test
    void hasCategory_WithUUID_ShouldReturnSpecification() {
        UUID categoryId = UUID.randomUUID();
        Specification<Event> spec = EventSpecifications.hasCategory(categoryId);

        assertNotNull(spec);
    }

    @Test
    void hasCategory_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasCategory(null);

        assertNull(spec);
    }

    @Test
    void hasOrganizer_WithUUID_ShouldReturnSpecification() {
        UUID organizerId = UUID.randomUUID();
        Specification<Event> spec = EventSpecifications.hasOrganizer(organizerId);

        assertNotNull(spec);
    }

    @Test
    void hasOrganizer_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasOrganizer(null);

        assertNull(spec);
    }

    @Test
    void hasSeats_WithTrue_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.hasSeats(true);

        assertNotNull(spec);
    }

    @Test
    void hasSeats_WithNull_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.hasSeats(null);

        assertNull(spec);
    }

    @Test
    void hasAvailableTickets_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.hasAvailableTickets();

        assertNotNull(spec);
    }

    @Test
    void withinGeoBoundingBox_WithAllParameters_ShouldReturnSpecification() {
        Specification<Event> spec = EventSpecifications.withinGeoBoundingBox(48.8566, 2.3522, 10.0);

        assertNotNull(spec);
    }

    @Test
    void withinGeoBoundingBox_WithNullLatitude_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.withinGeoBoundingBox(null, 2.3522, 10.0);

        assertNull(spec);
    }

    @Test
    void withinGeoBoundingBox_WithNullLongitude_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.withinGeoBoundingBox(48.8566, null, 10.0);

        assertNull(spec);
    }

    @Test
    void withinGeoBoundingBox_WithNullRadius_ShouldReturnNull() {
        Specification<Event> spec = EventSpecifications.withinGeoBoundingBox(48.8566, 2.3522, null);

        assertNull(spec);
    }
}
