package com.events.modules.event.repository;

import com.events.common.abstraction.SpringSecurityAuditorAware;
import com.events.common.config.JpaConfig;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.aggregate.PriceCategory;
import com.events.modules.event.enumeration.EventStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie sur une vraie base (H2) les requêtes atomiques de réservation de places.
 */
@DataJpaTest
@Import({JpaConfig.class, SpringSecurityAuditorAware.class})
class StockReservationEventTest {

    @Autowired private IEventRepository eventRepository;
    @Autowired private TestEntityManager entityManager;

    private UUID eventId;
    private UUID priceCategoryId;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        Event event = Event.builder()
                .name("Concert").description("Live").location("Paris")
                .isPublic(true).isFeatured(false).hasInvitationCode(false).hasSeats(false)
                .startDate(start).endDate(start.plusHours(3))
                .ticketSalesStartDate(LocalDateTime.now().minusDays(1))
                .status(EventStatusEnum.PUBLISHED)
                .build();
        PriceCategory regular = PriceCategory.builder()
                .name("REGULAR-" + UUID.randomUUID()).price(new BigDecimal("20.00"))
                .totalTickets(5).availableTickets(5).soldTickets(0)
                .event(event)
                .build();
        event.getPriceCategories().add(regular);
        entityManager.persist(event);
        entityManager.flush();
        eventId = event.getId();
        priceCategoryId = regular.getId();
        entityManager.clear();
    }

    @Test
    void reservesOnlyWhenEnoughTicketsRemain() {
        assertThat(eventRepository.reservePriceCategoryTickets(eventId, priceCategoryId, 3)).isEqualTo(1);
        assertThat(eventRepository.reservePriceCategoryTickets(eventId, priceCategoryId, 3)).isZero();
        assertThat(eventRepository.reservePriceCategoryTickets(eventId, priceCategoryId, 2)).isEqualTo(1);

        PriceCategory category = entityManager.find(PriceCategory.class, priceCategoryId);
        assertThat(category.getAvailableTickets()).isZero();
        assertThat(category.getSoldTickets()).isEqualTo(5);
    }

    @Test
    void releaseRestoresTicketsAndEventCounter() {
        eventRepository.reservePriceCategoryTickets(eventId, priceCategoryId, 4);
        eventRepository.adjustEventAvailableTickets(eventId, -4);

        assertThat(eventRepository.releasePriceCategoryTickets(eventId, priceCategoryId, 4)).isEqualTo(1);
        eventRepository.adjustEventAvailableTickets(eventId, 4);

        assertThat(entityManager.find(PriceCategory.class, priceCategoryId).getAvailableTickets()).isEqualTo(5);
        assertThat(entityManager.find(Event.class, eventId).getAvailableTickets()).isEqualTo(5);
    }

    @Test
    void cannotReserveOnAnotherEventsCategory() {
        assertThat(eventRepository.reservePriceCategoryTickets(UUID.randomUUID(), priceCategoryId, 1)).isZero();
    }
}
