package com.events.modules.event.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.event.dto.AddEventStaffDto;
import com.events.modules.event.dto.EventSearchCriteriaDto;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.dto.mapper.IEventMapper;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.aggregate.PriceCategory;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.events.EventCancelledEvent;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.impl.EventService;
import com.events.modules.user.entity.User;
import com.events.modules.user.enumeration.RoleEnum;
import com.events.modules.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Fonctionnalités organisateur d'EventService : annulation, suppression, staff, droits de contrôle.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganizerFeaturesEventTest {

    @Mock private IEventRepository eventRepository;
    @Mock private IAuthService authService;
    @Mock private IUserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private IEventMapper eventMapper;

    @InjectMocks private EventService eventService;

    private User organizer;
    private User stranger;
    private Event event;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(UUID.randomUUID()).role(RoleEnum.ORGANIZER).build();
        stranger = User.builder().id(UUID.randomUUID()).role(RoleEnum.ORGANIZER).build();
        event = Event.builder()
                .id(UUID.randomUUID())
                .name("Concert")
                .organizer(organizer)
                .status(EventStatusEnum.PUBLISHED)
                .staff(new HashSet<>())
                .build();
        event.getPriceCategories().add(PriceCategory.builder()
                .name("REGULAR").price(BigDecimal.TEN).totalTickets(10).availableTickets(10).soldTickets(0)
                .build());
        given(eventRepository.findById(event.getId())).willReturn(Optional.of(event));
        given(authService.getCurrentUser()).willReturn(organizer);
        given(authService.findCurrentUser()).willReturn(Optional.of(organizer));
    }

    @Test
    void organizerCancellationTriggersCascade() {
        eventService.cancelEvent(event.getId());

        assertThat(event.getStatus()).isEqualTo(EventStatusEnum.CANCELLED);
        verify(eventPublisher).publishEvent(new EventCancelledEvent(event.getId(), "Concert"));
    }

    @Test
    void anotherOrganizerCannotCancel() {
        given(authService.getCurrentUser()).willReturn(stranger);

        assertThatThrownBy(() -> eventService.cancelEvent(event.getId()))
                .isInstanceOf(EventForbidenException.class);

        assertThat(event.getStatus()).isEqualTo(EventStatusEnum.PUBLISHED);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void adminCanCancelAnyEvent() {
        given(authService.getCurrentUser()).willReturn(User.builder().id(UUID.randomUUID()).role(RoleEnum.ADMIN).build());

        eventService.cancelEvent(event.getId());

        assertThat(event.getStatus()).isEqualTo(EventStatusEnum.CANCELLED);
    }

    @Test
    void draftEventCannotBeCancelled() {
        event.setStatus(EventStatusEnum.DRAFT);

        assertThatThrownBy(() -> eventService.cancelEvent(event.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void eventWithSoldTicketsCannotBeDeleted() {
        event.getPriceCategories().iterator().next().setSoldTickets(3);

        assertThatThrownBy(() -> eventService.softDeleteEvent(event.getId()))
                .isInstanceOf(BadRequestException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void eventWithoutSalesCanBeDeleted() {
        eventService.softDeleteEvent(event.getId());

        assertThat(event.getIsActive()).isFalse();
    }

    @Test
    void staffMemberCanManageEntryButNotStrangers() {
        User staff = User.builder().id(UUID.randomUUID()).email("staff@example.com").role(RoleEnum.USER).build();
        given(userService.findByEmail("staff@example.com")).willReturn(staff);

        eventService.addStaff(event.getId(), new AddEventStaffDto("staff@example.com"));
        assertThat(event.getStaff()).containsExactly(staff);

        given(authService.getCurrentUser()).willReturn(staff);
        assertThat(eventService.isCurrentUserEntryManager(event.getId())).isTrue();

        given(authService.getCurrentUser()).willReturn(stranger);
        assertThat(eventService.isCurrentUserEntryManager(event.getId())).isFalse();
    }

    @Test
    void staffCannotAddOtherStaff() {
        User staff = User.builder().id(UUID.randomUUID()).role(RoleEnum.USER).build();
        event.setStaff(new HashSet<>(Set.of(staff)));
        given(authService.getCurrentUser()).willReturn(staff);

        assertThatThrownBy(() -> eventService.addStaff(event.getId(), new AddEventStaffDto("x@example.com")))
                .isInstanceOf(EventForbidenException.class);
    }

    // --- Catalogue public : les brouillons restent privés ------------------------------------

    @Test
    void visitorCannotOpenADraftEvent() {
        event.setStatus(EventStatusEnum.DRAFT);
        given(authService.findCurrentUser()).willReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(event.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void organizerCanOpenTheirOwnDraft() {
        event.setStatus(EventStatusEnum.DRAFT);
        given(eventMapper.toDto(event)).willReturn(mock(GetEventDto.class));

        eventService.getEventById(event.getId());

        verify(eventMapper).toDto(event);
    }

    @Test
    void anotherOrganizerCannotOpenADraft() {
        event.setStatus(EventStatusEnum.DRAFT);
        given(authService.findCurrentUser()).willReturn(Optional.of(stranger));

        assertThatThrownBy(() -> eventService.getEventById(event.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void publicSearchFiltersOnStatusForVisitorsOnly() {
        EventSearchCriteriaDto noCriteria = new EventSearchCriteriaDto(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);

        given(authService.findCurrentUser()).willReturn(Optional.empty());
        assertThat(statusIsFilteredFor(noCriteria)).isTrue();

        User admin = User.builder().id(UUID.randomUUID()).role(RoleEnum.ADMIN).build();
        given(authService.findCurrentUser()).willReturn(Optional.of(admin));
        assertThat(statusIsFilteredFor(noCriteria)).isFalse();
    }

    /** Exécute la recherche et évalue la spécification produite sur une requête JPA simulée. */
    @SuppressWarnings("unchecked")
    private boolean statusIsFilteredFor(EventSearchCriteriaDto criteria) {
        org.mockito.Mockito.reset(eventRepository);
        // any() (et non any(Specification.class)) : sans critère, la spécification peut être null
        given(eventRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Event>>any(), any(PageRequest.class)))
                .willReturn(new PageImpl<Event>(List.of()));
        eventService.getEvents(criteria, PageRequest.of(0, 10));

        var captor = org.mockito.ArgumentCaptor.forClass(Specification.class);
        verify(eventRepository).findAll(captor.capture(), any(PageRequest.class));
        if (captor.getValue() == null) {
            return false;
        }
        Root<Event> root = mock(Root.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        CriteriaQuery<?> query = mock(CriteriaQuery.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        CriteriaBuilder cb = mock(CriteriaBuilder.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        ((Specification<Event>) captor.getValue()).toPredicate(root, query, cb);
        return org.mockito.Mockito.mockingDetails(root).getInvocations().stream()
                .anyMatch(inv -> inv.getMethod().getName().equals("get") && "status".equals(inv.getArgument(0)));
    }
}
