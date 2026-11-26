package com.events.modules.ticket.repository;

import com.events.common.abstraction.SpringSecurityAuditorAware;
import com.events.common.config.JpaConfig;
import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie sur une vraie base (H2) que le check-in est atomique : un billet ne passe qu'une fois.
 */
@DataJpaTest
@Import({JpaConfig.class, SpringSecurityAuditorAware.class})
class CheckInTicketTest {

    @Autowired private ITicketRepository ticketRepository;
    @Autowired private TestEntityManager entityManager;

    private final UUID eventId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        User holder = User.builder().email("holder-" + UUID.randomUUID() + "@example.com").fullName("Holder").build();
        entityManager.persist(holder);
        entityManager.persist(ticket(holder, "code-valid", TicketStatusEnum.VALID));
        entityManager.persist(ticket(holder, "code-cancelled", TicketStatusEnum.CANCELLED));
        entityManager.flush();
        entityManager.clear();
    }

    private Ticket ticket(User holder, String code, TicketStatusEnum status) {
        return Ticket.builder()
                .eventId(eventId).user(holder).ticketCode(code).price(10.0).status(status)
                .build();
    }

    private int checkIn(String code, UUID event) {
        return ticketRepository.markAsUsed(code, event, staffId, LocalDateTime.now(),
                TicketStatusEnum.VALID, TicketStatusEnum.USED);
    }

    @Test
    void validTicketIsAcceptedOnlyOnce() {
        assertThat(checkIn("code-valid", eventId)).isEqualTo(1);
        assertThat(checkIn("code-valid", eventId)).isZero();

        Ticket used = ticketRepository.findByTicketCode("code-valid").orElseThrow();
        assertThat(used.getStatus()).isEqualTo(TicketStatusEnum.USED);
        assertThat(used.getCheckedInBy()).isEqualTo(staffId);
        assertThat(used.getCheckedInAt()).isNotNull();
    }

    @Test
    void cancelledTicketOrWrongEventIsRefused() {
        assertThat(checkIn("code-cancelled", eventId)).isZero();
        assertThat(checkIn("code-valid", UUID.randomUUID())).isZero();
    }

    @Test
    void statsCountByStatus() {
        checkIn("code-valid", eventId);
        assertThat(ticketRepository.countByEventIdAndStatus(eventId, TicketStatusEnum.USED)).isEqualTo(1);
        assertThat(ticketRepository.countByEventIdAndStatus(eventId, TicketStatusEnum.CANCELLED)).isEqualTo(1);
    }
}
