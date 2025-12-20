package com.events.modules.event.service;

import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.UpdateEventCommandDto;

import java.util.List;
import java.util.UUID;

public interface IEventService {
    UUID createEvent(CreateEventCommandDto command);
    List<EventDto> getAllEvents();
    EventDto getEventById(Long id);
    List<EventDto> getEventsNearby(Double lat, Double lon, Double radiusInMeters);
    void updateEvent(Long id, UpdateEventCommandDto command);
    List<EventDto> getAllIncomingEvents();
//TODO:void deleteEvent(Long id);
//TODO:List<EventDto> searchEvents(String keyword, String category, Double lat, Double lon, Double radiusInMeters);
//TODO:void addParticipant(Long eventId, Long userId);
//TODO:void removeParticipant(Long eventId, Long userId);
//TODO:List<Long> getParticipants(Long eventId);
//TODO:EventStatisticsDto getEventStatistics(Long eventId);
}