package com.events.modules.event.service.impl;

import com.events.common.supabase.service.ISupabaseStorageService;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.common.exception.BadRequestException;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.GenerateSeatDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.aggregate.Image;
import com.events.modules.event.entity.aggregate.Seat;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.dto.mapper.IEventMapper;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.IEventService;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class EventService implements IEventService {

    private final IEventRepository eventRepository;
    private final IAuthService authService;
    private final IEventMapper eventMapper;
    private final ISupabaseStorageService supabaseStorageService;

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

        if (!command.isFreeEntry() && (command.ticketPrice() == null || command.ticketPrice() <= 0)) {
            throw new BadRequestException(Constants.PRICE_MUST_BE_POSITIVE);
        }

        if (command.isFreeEntry() && (command.ticketPrice() != null)) {
            throw new BadRequestException(Constants.PRICE_MUST_BE_NULL_FOR_FREE_EVENT);
        }

        User currentUser = authService.getCurrentUser();

        Event event = Event.builder()
                .name(command.name())
                .description(command.description())
                .location(command.location())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .organizer(currentUser)
                .status(EventStatusEnum.DRAFT)
                .price(command.isFreeEntry() ? 0 : command.ticketPrice())
                .ticketSalesEndDate(command.ticketSalesStartDate())
                .ticketSalesStartDate(command.ticketSalesEndDate())
                .totalTickets(command.totalTickets())
                .availableTickets(command.totalTickets())
                .isFreeEntry(command.isFreeEntry())
                .isPublic(command.isPublic())
                .hasSeats(command.hasSeats())
                .hasInvitationCode(command.hasInvitationCode())
                .build();

        eventRepository.save(event);

        return event.getId();
    }

    @Override
    public List<String> uploadEventImages(UUID eventId, MultipartFile[] files) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if(event.getImages().size() >  10){
            throw new BadRequestException("Maximum number of images reached for this event");
        }

        List<String> imagesUrls = supabaseStorageService.uploadFiles(files);
        Set<Image> images = imagesUrls.stream()
                .map(url -> Image.builder()
                        .url(url)
                        .build())
                .collect(java.util.stream.Collectors.toSet());

        event.getImages().addAll(images);

        return imagesUrls;
    }

    @Override
    public Page<EventDto> getAllEvents(int page, int size, String country) {
        Pageable pageable = PageRequest.of(page, size);
        var eventsListDto =  eventRepository.findAll(pageable).stream()
                .map(eventMapper::toDto).toList();

        return new PageImpl<>(eventsListDto);
    }

    @Override
    public EventDto getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(eventMapper::toDto)
                .orElseThrow(() -> new BadRequestException("Event not found"));
    }

    @Override
    public List<EventDto> getEventsNearby(Double lat, Double lon, Double radius) {
        return eventRepository.findByLocationNear(lat, lon, radius).stream()
                .map(eventMapper::toDto).toList();
    }

    @Override
    public void updateEvent(UUID id, UpdateEventCommandDto command) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new EventForbidenException(event.getId());
        }

        updateEvent(command, event);
    }

    @Override
    public List<EventDto> getAllIncomingEvents() {
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

    private static void updateEvent(UpdateEventCommandDto command, Event event) {
        if(command.totalTickets() < event.getAvailableTickets()) {
            throw new BadRequestException("Total tickets cannot be less than available tickets");
        }

        if (command.startDate().isAfter(command.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (command.ticketSalesStartDate().isAfter(command.ticketSalesEndDate())) {
            throw new BadRequestException("Ticket sales start date cannot be after end date");
        }

        event.setName(command.name());
        event.setDescription(command.description());
        event.setLocation(command.location());
        event.setStartDate(command.startDate());
        event.setTicketSalesStartDate(command.ticketSalesStartDate());
        event.setTicketSalesEndDate(command.ticketSalesEndDate());
        event.setStatus(command.status());
        event.setLatitude(command.latitude());
        event.setLongitude(command.longitude());
        event.setTotalTickets(command.totalTickets());
        event.setPrice(command.ticketPrice());
        event.setAvailableTickets(command.totalTickets());
    }

    @Override
    public void softDeleteEvent(UUID eventId) {
        User currentUser = authService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new EventForbidenException(event.getId());
        }

        event.setIsActive(false);
        eventRepository.save(event);
    }
}


