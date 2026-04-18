package com.events.modules.event.service;
//
//import com.events.common.exception.BadRequestException;
//import com.events.modules.auth.service.auth.IAuthService;
//import com.events.modules.category.dto.CategoryDto;
//import com.events.modules.category.dto.mapper.ICategoryMapper;
//import com.events.modules.category.entity.Category;
//import com.events.modules.category.service.ICategoryService;
//import com.events.modules.country.dto.CountryDto;
//import com.events.modules.country.dto.mapper.ICountryMapper;
//import com.events.modules.country.entity.Country;
//import com.events.modules.country.enumeration.PaysEnum;
//import com.events.modules.country.service.ICountryService;
//import com.events.modules.event.dto.*;
//import com.events.modules.event.dto.mapper.IEventMapper;
//import com.events.modules.event.entity.Event;
//import com.events.modules.event.entity.aggregate.PriceCategory;
//import com.events.modules.event.enumeration.EventStatusEnum;
//import com.events.modules.event.repository.IEventRepository;
//import com.events.modules.event.service.impl.EventService;
//import com.events.modules.event.dto.mapper.IPriceCategoryMapper;
//import com.events.modules.user.entity.User;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EventServiceTest {
//
//    @Mock
//    private IEventRepository eventRepository;
//
//    @Mock
//    private ICategoryService categoryService;
//
//    @Mock
//    private ICategoryMapper categoryMapper;
//
//    @Mock
//    private ICountryService countryService;
//
//    @Mock
//    private ICountryMapper countryMapper;
//
//    @Mock
//    private IAuthService authService;
//
//    @InjectMocks
//    private EventService eventService;
//
//    @Mock
//    private IEventMapper eventMapper;
//
//    @Mock
//    private IPriceCategoryMapper priceCategoryMapper;
//
//    @Mock
//    private com.events.modules.event.dto.mapper.IEventSearchMapper eventSearchMapper;
//
//
//    private CreateEventCommandDto createEventCommandDto;
//    private User organizer;
//    private Event event;
//    private Category category;
//    private CategoryDto categoryDto;
//    private Country country;
//    private CountryDto countryDto;
//    private UUID eventId;
//    private UUID organizerId;
//    private UUID categoryId;
//    private UUID countryId;
//
//
//    @BeforeEach
//    void setUp() {
//        organizerId = UUID.randomUUID();
//        organizer = new User();
//        organizer.setId(organizerId);
//
//        categoryId = UUID.randomUUID();
//        countryId = UUID.randomUUID();
//
//        category = Category.builder()
//                .name("Test Category")
//                .description("Test Description")
//                .build();
//
//        categoryDto = new CategoryDto(
//                categoryId,
//                "Test Category",
//                "Test Description"
//        );
//
//        country = Country.builder()
//                .name("Test Country")
//                .code("TC")
//                .build();
//
//        countryDto = new CountryDto(
//                countryId,
//                "France",
//                "FR",
//                PaysEnum.FRANCE
//        );
//
//        eventId = UUID.randomUUID();
//        event = new Event();
//        event.setId(eventId);
//        event.setOrganizer(organizer);
//        event.setStatus(EventStatusEnum.PUBLISHED);
//        event.setCategory(category);
//        event.setCountry(country);
//
//        createEventCommandDto = new CreateEventCommandDto(
//                "Test Event",
//                "Test Description",
//                true,
//                false,
//                false,
//                "false",
//                "Test Location",
//                0.0,
//                0.0,
//                LocalDateTime.now().plusDays(1),
//                LocalDateTime.now().plusDays(2),
//                100,
//                LocalDateTime.now(),
//                LocalDateTime.now().plusDays(1),
//                10.0,
//                false,
//                Collections.emptyList(),
//                Collections.emptyList(),
//                categoryId,
//                countryId
//        );
//    }
//
//    @Nested
//    class CreateEventTests {
////
////        @Test
////        void shouldCreateEventWithDefaultCategoriesWhenNoCategoriesAreProvided() {
////            when(authService.getCurrentUser()).thenReturn(organizer);
////            when(eventMapper.toEntity(any(CreateEventCommandDto.class))).thenReturn(new Event());
////
////            eventService.createEvent(createEventCommandDto);
////
////            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
////            verify(eventRepository).save(eventCaptor.capture());
////
////            Event savedEvent = eventCaptor.getValue();
////            assertEquals(4, savedEvent.getPriceCategories().size());
////        }
//
//        @Test
//        void shouldCreateEventWithCustomCategories() {
//            PriceCategoryDto priceCategoryDto = new PriceCategoryDto("CUSTOM", BigDecimal.TEN, 100);
//            createEventCommandDto = new CreateEventCommandDto(
//                    "Test Event",
//                    "Test Description",
//                    true,
//                    false,
//                    false,
//                    "false",
//                    "Test Location",
//                    0.0,
//                    0.0,
//                    LocalDateTime.now().plusDays(1),
//                    LocalDateTime.now().plusDays(2),
//                    100,
//                    LocalDateTime.now(),
//                    LocalDateTime.now().plusDays(1),
//                    10.0,
//                    false,
//                    Collections.emptyList(),
//                    List.of(priceCategoryDto),
//                    categoryId,
//                    countryId
//            );
//
//            PriceCategory customPriceCategory = PriceCategory.builder()
//                    .name("CUSTOM")
//                    .price(BigDecimal.TEN)
//                    .totalTickets(100)
//                    .availableTickets(100)
//                    .build();
//
//            Event eventToCreate = Event.builder()
//                    .availableTickets(100)
//                    .build();
//            eventToCreate.setPriceCategories(Collections.singleton(customPriceCategory));
//
//            // Mock all required service calls
//            when(categoryService.getCategoryById(categoryId)).thenReturn(categoryDto);
//            when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
//            when(countryService.getCountryById(countryId)).thenReturn(countryDto);
//            when(countryMapper.toEntity(countryDto)).thenReturn(country);
//            when(eventMapper.toEntity(any(CreateEventCommandDto.class))).thenReturn(eventToCreate);
//            when(authService.getCurrentUser()).thenReturn(organizer);
//
//            eventService.createEvent(createEventCommandDto);
//
//            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
//            verify(eventRepository).save(eventCaptor.capture());
//
//            Event savedEvent = eventCaptor.getValue();
//            assertEquals(1, savedEvent.getPriceCategories().size());
//            assertEquals("CUSTOM", savedEvent.getPriceCategories().iterator().next().getName());
//            assertEquals(category, savedEvent.getCategory());
//            assertEquals(country, savedEvent.getCountry());
//            assertEquals(organizer, savedEvent.getOrganizer());
//        }
//
//        @Test
//        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
//            createEventCommandDto = new CreateEventCommandDto(
//                    "Test Event",
//                    "Test Description",
//                    true,
//                    false,
//                    false,
//                    "false",
//                    "Test Location",
//                    0.0,
//                    0.0,
//                    LocalDateTime.now().plusDays(2),
//                    LocalDateTime.now().plusDays(1),
//                    100,
//                    LocalDateTime.now(),
//                    LocalDateTime.now().plusDays(1),
//                    10.0,
//                    false,
//                    Collections.emptyList(),
//                    Collections.emptyList(),
//                    categoryId,
//                    countryId
//            );
//
//            assertThrows(BadRequestException.class, () -> eventService.createEvent(createEventCommandDto));
//        }
//
//        @Test
//        void shouldThrowExceptionWhenTicketSalesStartDateIsAfterTicketSalesEndDate() {
//            createEventCommandDto = new CreateEventCommandDto(
//                    "Test Event",
//                    "Test Description",
//                    true,
//                    false,
//                    false,
//                    "false",
//                    "Test Location",
//                    0.0,
//                    0.0,
//                    LocalDateTime.now().plusDays(1),
//                    LocalDateTime.now().plusDays(2),
//                    100,
//                    LocalDateTime.now().plusDays(1),
//                    LocalDateTime.now(),
//                    10.0,
//                    false,
//                    Collections.emptyList(),
//                    Collections.emptyList(),
//                    categoryId,
//                    countryId
//            );
//
//            assertThrows(BadRequestException.class, () -> eventService.createEvent(createEventCommandDto));
//        }
//    }
//
//    @Nested
//    class CancelEventTests {
//
//        @Test
//        void shouldCancelEventSuccessfully() {
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.of(event));
//
//            eventService.cancelEvent(eventId);
//
//            assertEquals(EventStatusEnum.CANCELLED, event.getStatus());
//            verify(eventRepository, times(1)).save(event);
//        }
//
//        @Test
//        void shouldThrowExceptionWhenEventNotFoundOrNotOwnedByOrganizer() {
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.empty());
//
//            assertThrows(BadRequestException.class,
//                    () -> eventService.cancelEvent(eventId));
//
//            verify(eventRepository, never()).save(any(Event.class));
//        }
//
//        @Test
//        void shouldThrowExceptionWhenEventHasWrongStatus() {
//            event.setStatus(EventStatusEnum.DRAFT);
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.of(event));
//
//            assertThrows(BadRequestException.class,
//                    () -> eventService.cancelEvent(eventId));
//
//            verify(eventRepository, never()).save(any(Event.class));
//        }
//    }
//
//    @Nested
//    class PublishEventTests {
//
//        @BeforeEach
//        void setup() {
//            event.setStatus(EventStatusEnum.DRAFT); // Ensure event is in DRAFT status for publish tests
//        }
//
//        @Test
//        void shouldPublishEventSuccessfully() {
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.of(event));
//
//            eventService.publishEvent(eventId);
//
//            assertEquals(EventStatusEnum.PUBLISHED, event.getStatus());
//            verify(eventRepository, times(1)).save(event);
//        }
//
//        @Test
//        void shouldThrowExceptionWhenEventNotFoundOrNotOwnedByOrganizer() {
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.empty());
//
//            assertThrows(BadRequestException.class,
//                    () -> eventService.publishEvent(eventId));
//
//            verify(eventRepository, never()).save(any(Event.class));
//        }
//
//        @Test
//        void shouldThrowExceptionWhenEventHasWrongStatus() {
//            event.setStatus(EventStatusEnum.PUBLISHED); // Set to PUBLISHED to trigger wrong status
//            when(authService.getCurrentUser()).thenReturn(organizer);
//            when(eventRepository.findByIdAndOrganizerId(eventId, organizerId))
//                    .thenReturn(Optional.of(event));
//
//            BadRequestException exception = assertThrows(BadRequestException.class,
//                    () -> eventService.publishEvent(eventId));
//
//            assertEquals("Only events with DRAFT status can be published.", exception.getMessage());
//            verify(eventRepository, never()).save(any(Event.class));
//        }
//    }
//
//    @Nested
//    class SearchEventTests {
//
//        private EventSearchCriteriaDto criteria;
//        private Pageable pageable;
//        private Event testEvent;
//        private GetEventDto searchViewDto;
//        private Page<Event> eventPage;
//
//        @BeforeEach
//        void setUp() {
//            // Setup test event
//            testEvent = new Event();
//            testEvent.setId(UUID.randomUUID());
//            testEvent.setName("Test Event");
//            testEvent.setStatus(EventStatusEnum.PUBLISHED);
//            testEvent.setIsPublic(true);
//            testEvent.setCategory(category);
//            testEvent.setCountry(country);
//
//            // Setup search view DTO
//            searchViewDto = new GetEventDto(
//                    testEvent.getId(),
//                    "Test Event",
//                    "Test Description",
//                    LocalDateTime.now().plusDays(1),
//                    LocalDateTime.now().plusDays(2),
//                    "Test Location",
//                    0.0,
//                    0.0,
//                    false,
//                    100,
//                    100,
//                    EventStatusEnum.PUBLISHED,
//                    true,
//                    true,
//                    new GetEventDto.CategorySummaryDto(categoryId, "Test Category"),
//                    new GetEventDto.CountrySummaryDto(countryId, "Test Country", "TC")
//            );
//
//            // Setup pageable
//            pageable = PageRequest.of(0, 10);
//
//            // Setup event page
//            eventPage = new PageImpl<>(List.of(testEvent), pageable, 1);
//        }
//
//        // ===== Basic Search Functionality =====
//
//        @Test
//        void searchEvents_WithNoCriteria_ShouldCallRepositoryWithNullSpec() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null,null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            verify(eventRepository).findAll((Specification<Event>) isNull(), eq(pageable));
//        }
//
//        @Test
//        void searchEvents_WithSingleFilter_ShouldBuildSpecificationCorrectly() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null,true, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//            verify(eventSearchMapper).toSearchViewList(anyList());
//        }
//
//        @Test
//        void searchEvents_WithMultipleFilters_ShouldCombineWithAndLogic() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null,true, EventStatusEnum.PUBLISHED, null, null, null, null, null, null,
//                    countryId, null, null, null, null, categoryId, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void searchEvents_ShouldMapResultsToSearchViewDto() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(eventPage.getContent())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            verify(eventSearchMapper).toSearchViewList(eventPage.getContent());
//            assertEquals(1, result.getContent().size());
//            assertEquals(searchViewDto, result.getContent().get(0));
//        }
//
//        @Test
//        void searchEvents_ShouldReturnPagedResults() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            assertTrue(result instanceof Page);
//            assertEquals(1, result.getTotalElements());
//            assertEquals(1, result.getContent().size());
//        }
//
//        // ===== Pagination & Sorting =====
//
//        @Test
//        void searchEvents_WithPagination_ShouldPassPageableToRepository() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null,null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            Pageable customPageable = PageRequest.of(2, 20);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(customPageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, customPageable);
//
//            // Then
//            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//            verify(eventRepository).findAll((Specification<Event>) isNull(), pageableCaptor.capture());
//            assertEquals(customPageable, pageableCaptor.getValue());
//        }
//
//        @Test
//        void searchEvents_WithCustomPageSize_ShouldRespectSize() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null,null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            Pageable customPageable = PageRequest.of(0, 50);
//            Page<Event> customEventPage = new PageImpl<>(List.of(testEvent), customPageable, 1);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(customPageable))).thenReturn(customEventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, customPageable);
//
//            // Then
//            assertEquals(50, result.getSize());
//        }
//
//        @Test
//        void searchEvents_ShouldReturnCorrectTotalElements() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            Page<Event> largeEventPage = new PageImpl<>(List.of(testEvent), pageable, 100);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(largeEventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertEquals(100, result.getTotalElements());
//        }
//
//        // ===== Specification Building Logic =====
//
//        @Test
//        void buildSpecification_WithNullCriteria_ShouldReturnNullSpec() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll((Specification<Event>) isNull(), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithPublicFilter_ShouldIncludePublicSpec() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    true, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithStatusFilter_ShouldIncludeStatusSpec() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, EventStatusEnum.PUBLISHED, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithUpcomingOnly_ShouldUseCurrentDateTime() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, true,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithDateRange_ShouldIncludeDateSpecs() {
//            // Given
//            LocalDateTime startFrom = LocalDateTime.now();
//            LocalDateTime startTo = LocalDateTime.now().plusDays(7);
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, startFrom, startTo, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithLocationFilters_ShouldIncludeLocationSpecs() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    countryId, "Paris", 48.8566, 2.3522, 10.0, null, null, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithCategorizationFilters_ShouldIncludeCatOrgSpecs() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, categoryId, organizerId, null, null
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        @Test
//        void buildSpecification_WithTicketingFilters_ShouldIncludeTicketSpecs() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, true, true
//            );
//
//            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            verify(eventRepository).findAll(any(Specification.class), eq(pageable));
//        }
//
//        // ===== Edge Cases =====
//
//        @Test
//        void searchEvents_WithEmptyRepositoryResult_ShouldReturnEmptyPage() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            Page<Event> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(emptyPage);
//            when(eventSearchMapper.toSearchViewList(Collections.emptyList())).thenReturn(Collections.emptyList());
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(0, result.getTotalElements());
//            assertTrue(result.getContent().isEmpty());
//        }
//
//        @Test
//        void searchEvents_WithNullPageable_ShouldThrowException() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            // When & Then
//            assertThrows(Exception.class, () -> eventService.getEvents(criteria, null));
//        }
//
//        @Test
//        void searchEvents_WithRepositoryException_ShouldPropagateException() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable)))
//                    .thenThrow(new RuntimeException("Database error"));
//
//            // When & Then
//            assertThrows(RuntimeException.class, () -> eventService.getEvents(criteria, pageable));
//        }
//
//        @Test
//        void searchEvents_WithMapperReturningNull_ShouldHandleGracefully() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(null);
//
//            // When & Then - PageImpl constructor throws IllegalArgumentException when content is null
//            assertThrows(IllegalArgumentException.class, () -> eventService.getEvents(criteria, pageable));
//        }
//
//        // ===== DTO Mapping Verification =====
//
//        @Test
//        void searchEvents_ShouldCallMapperWithEventList() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            eventService.getEvents(criteria, pageable);
//
//            // Then
//            ArgumentCaptor<List<Event>> eventListCaptor = ArgumentCaptor.forClass(List.class);
//            verify(eventSearchMapper).toSearchViewList(eventListCaptor.capture());
//            assertEquals(eventPage.getContent(), eventListCaptor.getValue());
//        }
//
//        @Test
//        void searchEvents_ShouldReturnMappedSearchViewDtos() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            List<GetEventDto> mappedDtos = List.of(searchViewDto);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(pageable))).thenReturn(eventPage);
//            when(eventSearchMapper.toSearchViewList(eventPage.getContent())).thenReturn(mappedDtos);
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, pageable);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(mappedDtos, result.getContent());
//        }
//
//        @Test
//        void searchEvents_ShouldPreservePageMetadata() {
//            // Given
//            criteria = new EventSearchCriteriaDto(
//                    null, null, null, null, null, null, null, null,
//                    null, null, null, null, null, null, null, null, null
//            );
//            Pageable customPageable = PageRequest.of(3, 25);
//            Page<Event> customEventPage = new PageImpl<>(List.of(testEvent), customPageable, 150);
//
//            when(eventRepository.findAll((Specification<Event>) isNull(), eq(customPageable))).thenReturn(customEventPage);
//            when(eventSearchMapper.toSearchViewList(anyList())).thenReturn(List.of(searchViewDto));
//
//            // When
//            Page<GetEventDto> result = eventService.getEvents(criteria, customPageable);
//
//            // Then
//            assertEquals(3, result.getNumber());
//            assertEquals(25, result.getSize());
//            assertEquals(150, result.getTotalElements());
//            assertEquals(6, result.getTotalPages());
//        }
//    }
//
////    @Nested
////    class UpdateEventTests {
////
////        @Test
////        void shouldUpdateEventCategories() {
////            // Given
////            UpdateEventCommandDto command = new UpdateEventCommandDto(
////                    "Updated Name",
////                    "Updated Desc",
////                    "Updated Loc",
////                    1.0, 1.0,
////                    LocalDateTime.now().plusDays(5),
////                    LocalDateTime.now().plusDays(6),
////                    LocalDateTime.now().plusDays(1),
////                    LocalDateTime.now().plusDays(2),
////                    200,
////                    20.0,
////                    EventStatusEnum.DRAFT,
////                    List.of(new PriceCategoryDto(null, "NEW_CAT", BigDecimal.ONE, 100))
////            );
////
////            Event eventToUpdate = eventMapper.toEntity();
////            eventToUpdate.setId(eventId);
////            eventToUpdate.setOrganizer(organizer);
////
////
////            when(authService.getCurrentUser()).thenReturn(organizer);
////            when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventToUpdate));
////            when(priceCategoryMapper.toEntityList(anyList())).thenReturn(List.of(PriceCategory.builder().name("NEW_CAT").price(BigDecimal.ONE).build()));
////
////            // When
////            eventService.updateEvent(eventId, command);
////
////            // Then
////            assertEquals(1, eventToUpdate.getPriceCategories().size());
////            assertEquals("NEW_CAT", eventToUpdate.getPriceCategories().iterator().next().getName());
////        }
////    }
//
//
//}
