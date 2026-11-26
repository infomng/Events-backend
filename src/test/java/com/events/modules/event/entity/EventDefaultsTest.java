package com.events.modules.event.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EventDefaultsTest {

    @Test
    void flagsMissingFromTheCreateCommandDefaultBeforePersist() {
        Event event = new Event();

        // Callback @PrePersist : sans valeur par défaut, l'insertion échoue sur les colonnes NOT NULL
        ReflectionTestUtils.invokeMethod(event, "compute");

        assertThat(event.getIsFeatured()).isFalse();
        assertThat(event.getHasSeats()).isFalse();
        assertThat(event.getHasInvitationCode()).isFalse();
        assertThat(event.getIsPublic()).isTrue();
        assertThat(event.getTicketSalesStartDate()).isNotNull();
        assertThat(event.getIsTicketSalesActive()).isTrue();
    }

    @Test
    void explicitValuesAreKept() {
        Event event = new Event();
        event.setIsFeatured(true);
        event.setHasSeats(true);

        ReflectionTestUtils.invokeMethod(event, "compute");

        assertThat(event.getIsFeatured()).isTrue();
        assertThat(event.getHasSeats()).isTrue();
    }
}
