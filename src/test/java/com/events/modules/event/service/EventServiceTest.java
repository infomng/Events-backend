package com.events.modules.event.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.impl.EventService;
import com.events.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private IEventRepository eventRepository;

    @Mock
    private IAuthService authService;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private Event event;
    private UUID eventId;
    private UUID organizerId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        organizer = new User();
        organizer.setId(organizerId);

        eventId = UUID.randomUUID();
        event = new Event();
        event.setId(eventId);
        event.setOrganizer(organizer);
        event.setStatus(EventStatusEnum.PUBLISHED);
    }

    @Nested
    class CancelEventTests {

        @Test
        void shouldCancelEventSuccessfully() {
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.of(event));

            eventService.cancelEvent(eventId);

            assertEquals(EventStatusEnum.CANCELLED, event.getStatus());
            verify(eventRepository, times(1)).save(event);
        }

        @Test
        void shouldThrowExceptionWhenEventNotFoundOrNotOwnedByOrganizer() {
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.empty());

            assertThrows(BadRequestException.class,
                    () -> eventService.cancelEvent(eventId));

            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        void shouldThrowExceptionWhenEventHasWrongStatus() {
            event.setStatus(EventStatusEnum.DRAFT);
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class,
                    () -> eventService.cancelEvent(eventId));

            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    class PublishEventTests {

        @BeforeEach
        void setup() {
            event.setStatus(EventStatusEnum.DRAFT); // Ensure event is in DRAFT status for publish tests
        }

        @Test
        void shouldPublishEventSuccessfully() {
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.of(event));

            eventService.publishEvent(eventId);

            assertEquals(EventStatusEnum.PUBLISHED, event.getStatus());
            verify(eventRepository, times(1)).save(event);
        }

        @Test
        void shouldThrowExceptionWhenEventNotFoundOrNotOwnedByOrganizer() {
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.empty());

            assertThrows(BadRequestException.class,
                    () -> eventService.publishEvent(eventId));

            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        void shouldThrowExceptionWhenEventHasWrongStatus() {
            event.setStatus(EventStatusEnum.PUBLISHED); // Set to PUBLISHED to trigger wrong status
            when(authService.getCurrentUser()).thenReturn(organizer);
            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
                    .thenReturn(Optional.of(event));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> eventService.publishEvent(eventId));

            assertEquals("Only events with DRAFT status can be published.", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }
    }


}
