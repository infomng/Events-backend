package com.events.modules.event.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.category.dto.CategoryDto;
import com.events.modules.category.dto.mapper.ICategoryMapper;
import com.events.modules.category.entity.Category;
import com.events.modules.category.service.ICategoryService;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.country.entity.Country;
import com.events.modules.country.enumeration.PaysEnum;
import com.events.modules.country.service.ICountryService;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.PriceCategoryDto;
import com.events.modules.event.dto.mapper.IEventMapper;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.PriceCategory;
import com.events.modules.event.entity.aggregate.Reservation;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.impl.EventService;
import com.events.modules.event.dto.mapper.IPriceCategoryMapper;
import com.events.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private IEventRepository eventRepository;

    @Mock
    private ICategoryService categoryService;

    @Mock
    private ICategoryMapper categoryMapper;

    @Mock
    private ICountryService countryService;

    @Mock
    private ICountryMapper countryMapper;

    @Mock
    private IAuthService authService;

    @InjectMocks
    private EventService eventService;

    @Mock
    private IEventMapper eventMapper;

    @Mock
    private IPriceCategoryMapper priceCategoryMapper;


    private CreateEventCommandDto createEventCommandDto;
    private User organizer;
    private Event event;
    private Category category;
    private CategoryDto categoryDto;
    private Country country;
    private CountryDto countryDto;
    private UUID eventId;
    private UUID organizerId;
    private UUID categoryId;
    private UUID countryId;


    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        organizer = new User();
        organizer.setId(organizerId);

        categoryId = UUID.randomUUID();
        countryId = UUID.randomUUID();

        category = Category.builder()
                .name("Test Category")
                .description("Test Description")
                .build();

        categoryDto = new CategoryDto(
                categoryId,
                "Test Category",
                "Test Description",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        country = Country.builder()
                .name("Test Country")
                .code("TC")
                .build();

        countryDto = new CountryDto(
                countryId,
                "France",
                "FR",
                PaysEnum.FRANCE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        eventId = UUID.randomUUID();
        event = new Event();
        event.setId(eventId);
        event.setOrganizer(organizer);
        event.setStatus(EventStatusEnum.PUBLISHED);
        event.setCategory(category);
        event.setCountry(country);

        createEventCommandDto = new CreateEventCommandDto(
                "Test Event",
                "Test Description",
                true,
                false,
                false,
                "false",
                "Test Location",
                0.0,
                0.0,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                100,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                10.0,
                false,
                Collections.emptyList(),
                Collections.emptyList(),
                categoryId,
                countryId
        );
    }

    @Nested
    class CreateEventTests {
//
//        @Test
//        void shouldCreateEventWithDefaultCategoriesWhenNoCategoriesAreProvided() {
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventMapper.toEntity(any(CreateEventCommandDto.class))).thenReturn(new Event());
//
//            eventService.createEvent(createEventCommandDto);
//
//            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
//            verify(eventRepository).save(eventCaptor.capture());
//
//            Event savedEvent = eventCaptor.getValue();
//            assertEquals(4, savedEvent.getPriceCategories().size());
//        }

        @Test
        void shouldCreateEventWithCustomCategories() {
            PriceCategoryDto priceCategoryDto = new PriceCategoryDto("CUSTOM", BigDecimal.TEN, 100);
            createEventCommandDto = new CreateEventCommandDto(
                    "Test Event",
                    "Test Description",
                    true,
                    false,
                    false,
                    "false",
                    "Test Location",
                    0.0,
                    0.0,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(2),
                    100,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1),
                    10.0,
                    false,
                    Collections.emptyList(),
                    List.of(priceCategoryDto),
                    categoryId,
                    countryId
            );

            PriceCategory customPriceCategory = PriceCategory.builder()
                    .name("CUSTOM")
                    .price(BigDecimal.TEN)
                    .totalTickets(100)
                    .availableTickets(100)
                    .build();

            Event eventToCreate = Event.builder()
                    .availableTickets(100)
                    .build();
            eventToCreate.setPriceCategories(Collections.singleton(customPriceCategory));

            // Mock all required service calls
            when(categoryService.getCategoryById(categoryId)).thenReturn(categoryDto);
            when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
            when(countryService.getCountryById(countryId)).thenReturn(countryDto);
            when(countryMapper.toEntity(countryDto)).thenReturn(country);
            when(eventMapper.toEntity(any(CreateEventCommandDto.class))).thenReturn(eventToCreate);
            when(authService.getCurrentUser()).thenReturn(organizer);

            eventService.createEvent(createEventCommandDto);

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());

            Event savedEvent = eventCaptor.getValue();
            assertEquals(1, savedEvent.getPriceCategories().size());
            assertEquals("CUSTOM", savedEvent.getPriceCategories().iterator().next().getName());
            assertEquals(category, savedEvent.getCategory());
            assertEquals(country, savedEvent.getCountry());
            assertEquals(organizer, savedEvent.getOrganizer());
        }

        @Test
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            createEventCommandDto = new CreateEventCommandDto(
                    "Test Event",
                    "Test Description",
                    true,
                    false,
                    false,
                    "false",
                    "Test Location",
                    0.0,
                    0.0,
                    LocalDateTime.now().plusDays(2),
                    LocalDateTime.now().plusDays(1),
                    100,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1),
                    10.0,
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    categoryId,
                    countryId
            );

            assertThrows(BadRequestException.class, () -> eventService.createEvent(createEventCommandDto));
        }

        @Test
        void shouldThrowExceptionWhenTicketSalesStartDateIsAfterTicketSalesEndDate() {
            createEventCommandDto = new CreateEventCommandDto(
                    "Test Event",
                    "Test Description",
                    true,
                    false,
                    false,
                    "false",
                    "Test Location",
                    0.0,
                    0.0,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(2),
                    100,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now(),
                    10.0,
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    categoryId,
                    countryId
            );

            assertThrows(BadRequestException.class, () -> eventService.createEvent(createEventCommandDto));
        }
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

//    @Nested
//    class UpdateEventTests {
//
//        @Test
//        void shouldUpdateEventCategories() {
//            // Given
//            UpdateEventCommandDto command = new UpdateEventCommandDto(
//                    "Updated Name",
//                    "Updated Desc",
//                    "Updated Loc",
//                    1.0, 1.0,
//                    LocalDateTime.now().plusDays(5),
//                    LocalDateTime.now().plusDays(6),
//                    LocalDateTime.now().plusDays(1),
//                    LocalDateTime.now().plusDays(2),
//                    200,
//                    20.0,
//                    EventStatusEnum.DRAFT,
//                    List.of(new PriceCategoryDto(null, "NEW_CAT", BigDecimal.ONE, 100))
//            );
//
//            Event eventToUpdate = eventMapper.toEntity();
//            eventToUpdate.setId(eventId);
//            eventToUpdate.setOrganizer(organizer);
//
//
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventToUpdate));
//            when(priceCategoryMapper.toEntityList(anyList())).thenReturn(List.of(PriceCategory.builder().name("NEW_CAT").price(BigDecimal.ONE).build()));
//
//            // When
//            eventService.updateEvent(eventId, command);
//
//            // Then
//            assertEquals(1, eventToUpdate.getPriceCategories().size());
//            assertEquals("NEW_CAT", eventToUpdate.getPriceCategories().iterator().next().getName());
//        }
//    }


}
