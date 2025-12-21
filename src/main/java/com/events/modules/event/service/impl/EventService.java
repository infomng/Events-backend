package com.events.modules.event.service.impl;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.common.exception.BadRequestException;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.dto.mapper.IEventMapper;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.event.service.IEventService;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class EventService implements IEventService {

    private final IEventRepository eventRepository;
    private final IAuthService authService;
    private final IEventMapper eventMapper;

    @Override
    public UUID createEvent(CreateEventCommandDto command) {

        User currentUser = authService.getCurrentUser();

        Event event = buildEvent(command, currentUser);

        eventRepository.save(event);

        return event.getId();
    }

    private static Event buildEvent(CreateEventCommandDto command, User currentUser) {
        return Event.builder()
                .name(command.name())
                .description(command.description())
                .location(command.location())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .price(command.isFree() ? 0 : command.ticketPrice())
                .ticketSalesEndDate(command.ticketSalesStartDate())
                .ticketSalesStartDate(command.ticketSalesEndDate())
                .organizer(currentUser)
                .totalTickets(command.totalTickets())
                .availableTickets(command.totalTickets())
                .hasSits(command.hasSeats())
                .availableTickets(command.totalTickets())
                .isFree(command.isFree())
                .status(EventStatusEnum.DRAFT)
                .isPublic(command.isPublic())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .isFreeEntry(command.isFree() && command.isFreeEntry())
                .build();
    }

    @Override
    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto).toList();
    }

    @Override
    public EventDto getEventById(Long id) {
        return eventRepository.findById(id)
                .map(eventMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Override
    public List<EventDto> getEventsNearby(Double lat, Double lon, Double radius) {
        return eventRepository.findByLocationNear(lat, lon, radius).stream()
                .map(eventMapper::toDto).toList();
    }

    @Override
    public void updateEvent(Long id, UpdateEventCommandDto command) {
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
}


