package com.events.modules.event.service.impl;

import com.events.common.dto.pagination.PageResponse;
import com.events.common.supabase.service.ISupabaseStorageService;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.common.exception.BadRequestException;
import com.events.modules.category.dto.CategoryDto;
import com.events.modules.category.dto.mapper.ICategoryMapper;
import com.events.modules.category.entity.Category;
import com.events.modules.category.service.ICategoryService;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.country.entity.Country;
import com.events.modules.country.service.ICountryService;
import com.events.modules.event.dto.*;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.aggregate.PriceCategory;
import com.events.modules.event.entity.aggregate.Image;
import com.events.modules.event.entity.aggregate.Seat;
import com.events.modules.event.enumeration.DefaultPriceCategoryEnum;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.dto.mapper.IEventMapper;
import com.events.modules.event.dto.mapper.IEventSearchMapper;
import com.events.modules.event.exception.MaximumNumberOfImageException;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.IEventService;
import com.events.modules.event.dto.mapper.IPriceCategoryMapper;
import com.events.modules.event.specification.EventSpecifications;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService implements IEventService {

    private final IEventRepository eventRepository;
    private final ICategoryService categoryService;
    private final ICategoryMapper categoryMapper;
    private final IAuthService authService;
    private final IEventMapper eventMapper;
    private final ISupabaseStorageService supabaseStorageService;
    private final IPriceCategoryMapper priceCategoryMapper;
    private final ICountryService countryService;
    private final ICountryMapper countryMapper;

    @Override
    public UUID createEvent(CreateEventCommandDto command) {
        if (command.startDate().isAfter(command.endDate())) {
            throw new BadRequestException(Constants.INVALID_DATE);
        }

        if (command.ticketSalesStartDate() != null
                && command.ticketSalesEndDate() != null
                && command.ticketSalesStartDate().isAfter(command.ticketSalesEndDate())) {
            throw new BadRequestException(Constants.INVALID_START_DATE);
        }

        // Fetch and validate category via CategoryService
        CategoryDto categoryDto = categoryService.getCategoryById(command.categoryId());
        Category category = categoryMapper.toEntity(categoryDto);

        CountryDto countryDto = countryService.getCountryById(command.countryId());
        Country country = countryMapper.toEntity(countryDto);

        Event event = eventMapper.toEntity(command);

        // Link event to category
        event.setCategory(category);
        event.setCountry(country);

        event.getPriceCategories().forEach(priceCategory -> priceCategory.setEvent(event));
        User currentUser = authService.getCurrentUser();
        event.setOrganizer(currentUser);

        eventRepository.save(event);

        return event.getId();
    }

    @Override
    public List<String> uploadEventImages(UUID eventId, MultipartFile[] files) throws IOException {

        User currentUser = authService.getCurrentUser();
        Event event = eventRepository.findByIdAndOrganizerId(eventId, currentUser.getId())
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if(event.getImages().size() >  Constants.MAXIMUM_NUMBER_OF_IMAGES){
            throw new MaximumNumberOfImageException(Constants.MAXIMUM_NUMBER_OF_IMAGES);
        }

        List<String> imagesUrls = supabaseStorageService.uploadFiles(files);
        Set<Image> images = imagesUrls.stream()
                .map(url -> Image.builder()
                        .url(url)
                        .build())
                .collect(Collectors.toSet());

        event.getImages().addAll(images);

        return imagesUrls;
    }

    @Override
    @Cacheable(value = "EVENTS", key = "#page + '-' + #size + '-' + #country")
    public PageResponse<GetEventDto> getAllEvents(int page, int size, String country) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsList = eventRepository.findAll(pageable);

        var eventsListDto = eventsList.map(eventMapper::toDto).toList();

        return PageResponse.<GetEventDto>builder()
                .page(page)
                .size(size)
                .totalElements(eventsList.getTotalElements())
                .totalPages(eventsList.getTotalPages())
                .last(eventsList.isLast())
                .content(eventsListDto).build();

    }

    @Override
    @Cacheable(value = "EVENT", key = "#id")
    public GetEventDto getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(eventMapper::toDto)
                .orElseThrow(() -> new BadRequestException("Event not found"));
    }

    @Override
    public List<GetEventDto> getEventsNearby(Double lat, Double lon, Double radius) {
        return eventRepository.findByLocationNear(lat, lon, radius).stream()
                .map(eventMapper::toDto).toList();
    }

    @Override
    @CachePut(value = "EVENT", key = "#id")
    public GetEventDto updateEvent(UUID id, UpdateEventCommandDto command) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new EventForbidenException(event.getId());
        }

        updateEvent(command, event);
        eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Override
    public List<GetEventDto> getAllIncomingEvents() {
        List<Event> events = eventRepository.getAllIncomingEvents();
        return eventMapper.toDtoList(events);
    }

    @Override
    public void generateSeatsForEvent(UUID id, List<GenerateSeatDto> seatsCommand) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if(Boolean.FALSE.equals(event.getHasSeats())){
            throw new BadRequestException("this event does not support seats");
        }

        if (!event.getSeats().isEmpty()) {
            throw new BadRequestException("Seats have already been generated for this event");
        }

        Set<Seat> seats = seatsCommand.stream().map(seatCommand ->
                Seat.builder()
                        .seatNumber(seatCommand.seatNumber())
                        .price(seatCommand.price())
                        .section(seatCommand.section())
                        .rowNumber(seatCommand.rowNumber())
                        .seatType(seatCommand.seatType())
                        .build())
                .collect(Collectors.toSet());

        event.setSeats(seats);
    }

    private void updateEvent(UpdateEventCommandDto command, Event event) {
        if(command.totalTickets() != null && command.totalTickets() < event.getAvailableTickets()) {
            throw new BadRequestException("Total tickets cannot be less than available tickets");
        }

        if (command.startDate() != null && command.startDate().isAfter(command.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (command.ticketSalesStartDate() != null && command.ticketSalesStartDate().isAfter(command.ticketSalesEndDate())) {
            throw new BadRequestException("Ticket sales start date cannot be after end date");
        }

        eventMapper.updateEvent(command, event);

        // Update category if provided
        if (command.categoryId() != null) {
            CategoryDto categoryDto = categoryService.getCategoryById(command.categoryId());
            Category category = categoryMapper.toEntity(categoryDto);
            event.setCategory(category);
        }

        if (command.categories() != null) {
            if (command.categories().isEmpty()) {
                throw new BadRequestException("Event must have at least one category.");
            }
            Set<PriceCategory> updatedCategories = new HashSet<>(priceCategoryMapper.toEntityList(command.categories()));
            updatedCategories.forEach(category -> category.setEvent(event));

            event.getPriceCategories().clear();
            event.getPriceCategories().addAll(updatedCategories);
        }
    }

    @Override
    @CacheEvict(value = "EVENT", key = "#id")
    public void softDeleteEvent(UUID eventId) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findByIdAndOrganizerId(eventId, currentUser.getId())
                .orElseThrow(() -> new EventForbidenException(eventId));

        event.setIsActive(false);
        eventRepository.save(event);
    }

    @Override
    public void cancelEvent(UUID eventId) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findByIdAndOrganizerId(eventId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException(Constants.EVENTS_NOT_FOUND));

        if (event.getStatus() != EventStatusEnum.PUBLISHED) {
            throw new BadRequestException("Event cannot be cancelled as it is not in PUBLISHED state.");
        }

        event.setStatus(EventStatusEnum.CANCELLED);
        eventRepository.save(event);

        // TODO: Notify all participants about the event cancellation

        // TODO: Trigger refunds for all sold tickets
    }

    @Override
    public void publishEvent(UUID eventId) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findByIdAndOrganizerId(eventId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException(Constants.EVENTS_NOT_FOUND));

        if (event.getStatus() != EventStatusEnum.DRAFT) {
            throw new BadRequestException(Constants.ONLY_EVENTS_WITH_DRAFT_STATUS_CAN_BE_PUBLISHED);
        }

        event.setStatus(EventStatusEnum.PUBLISHED);
        eventRepository.save(event);
    }

    @Override
    public List<String> getDefaultPriceCategories() {
        return List.of(DefaultPriceCategoryEnum.FREE.name(), DefaultPriceCategoryEnum.REGULAR.name(),
                DefaultPriceCategoryEnum.VIP.name(), DefaultPriceCategoryEnum.PREMIUM.name());
    }

    @Override
    public void saveAll(List<CreateEventCommandDto> events) {
        List<Event> eventEntities = events.stream()
                .map(eventMapper::toEntity)
                .toList();

        eventRepository.saveAll(eventEntities);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "eventSearch",
            key = "#criteria.toString() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            condition = "#criteria.isPublic() == true && #pageable.pageNumber == 0"
    )
    public PageResponse<GetEventDto> getEvents(EventSearchCriteriaDto criteria, Pageable pageable) {
        log.debug("Searching events with criteria: {}", criteria);

        Specification<Event> spec = buildSpecification(criteria);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        List<GetEventDto> eventDtoList = eventMapper.toDtoList(events.getContent());

        return PageResponse.<GetEventDto>builder()
                .content(eventDtoList)
                .totalElements(events.getTotalElements())
                .totalPages(events.getTotalPages())
                .size(events.getNumber())
                .last(events.isLast())
                .build();
    }

    @Override
    public Boolean existById(UUID id) {
        return eventRepository.existsById(id);
    }

    @Override
    public List<GetEventDto> getAllByIds(List<UUID> ids) {
        return eventMapper.toDtoList(eventRepository.findAllById(ids));
    }

    /**
     * Build the combined JPA Specification from search criteria.
     *
     * @param criteria the search criteria
     * @return the combined specification
     */
    private Specification<Event> buildSpecification(EventSearchCriteriaDto criteria) {
        List<Specification<Event>> specs = new ArrayList<>();

        // Visibility & state filters
        addSpecIfNotNull(specs, EventSpecifications.isFeatured(criteria.isFeatured()));
        addSpecIfNotNull(specs, EventSpecifications.isPublic(criteria.isPublic()));
        addSpecIfNotNull(specs, EventSpecifications.hasStatus(criteria.status()));
        addSpecIfNotNull(specs, EventSpecifications.isTicketSalesActive(criteria.isTicketSalesActive()));

        // Date filters
        if (Boolean.TRUE.equals(criteria.upcomingOnly())) {
            addSpecIfNotNull(specs, EventSpecifications.startsAfter(LocalDateTime.now()));
        } else {
            addSpecIfNotNull(specs, EventSpecifications.startsAfter(criteria.startDateFrom()));
            addSpecIfNotNull(specs, EventSpecifications.startsBefore(criteria.startDateTo()));
        }
        addSpecIfNotNull(specs, EventSpecifications.endsAfter(criteria.endDateFrom()));
        addSpecIfNotNull(specs, EventSpecifications.endsBefore(criteria.endDateTo()));

        // Location filters
        addSpecIfNotNull(specs, EventSpecifications.hasCountry(criteria.countryId()));
        addSpecIfNotNull(specs, EventSpecifications.hasLocation(criteria.location()));
        addSpecIfNotNull(specs, EventSpecifications.withinGeoBoundingBox(
                criteria.latitude(), criteria.longitude(), criteria.radiusKm()
        ));

        // Categorization filters
        addSpecIfNotNull(specs, EventSpecifications.hasCategory(criteria.categoryId()));
        addSpecIfNotNull(specs, EventSpecifications.hasOrganizer(criteria.organizerId()));

        // Ticketing filters
        addSpecIfNotNull(specs, EventSpecifications.hasSeats(criteria.hasSeats()));
        if (Boolean.TRUE.equals(criteria.hasAvailableTickets())) {
            addSpecIfNotNull(specs, EventSpecifications.hasAvailableTickets());
        }

        // Combine all specifications with AND
        return specs.stream()
                .reduce(Specification::and)
                .orElse(null);
    }

    /**
     * Add a specification to the list if it's not null.
     *
     * @param specs the list of specifications
     * @param spec  the specification to add
     */
    private void addSpecIfNotNull(List<Specification<Event>> specs, Specification<Event> spec) {
        if (spec != null) {
            specs.add(spec);
        }
    }
}


