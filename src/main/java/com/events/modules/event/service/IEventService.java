package com.events.modules.event.service;

import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.GenerateSeatDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IEventService {
    UUID createEvent(CreateEventCommandDto command);
    List<String> uploadEventImages(UUID eventId, MultipartFile[] files) throws IOException;
    Page<EventDto> getAllEvents(int page, int size, String country);


    EventDto getEventById(UUID id);
    List<EventDto> getEventsNearby(Double lat, Double lon, Double radiusInMeters);
    void updateEvent(UUID id, UpdateEventCommandDto command);
    List<EventDto> getAllIncomingEvents();

    void generateSeatsForEvent(UUID id, List<GenerateSeatDto> seatNumbers);
//TODO:void deleteEvent(Long id);
//TODO:List<EventDto> searchEvents(String keyword, String category, Double lat, Double lon, Double radiusInMeters);
//TODO:void addParticipant(Long eventId, Long userId);
//TODO:void removeParticipant(Long eventId, Long userId);
//TODO:List<EventDto> getParticipants(Long eventId);
//TODO:EventStatisticsDto getEventStatistics(Long eventId);
    void softDeleteEvent(UUID eventId);
        void cancelEvent(UUID eventId);
        void publishEvent(UUID eventId);

    List<String> getDefaultPriceCategories();

    List<String> getCountries();
}
    