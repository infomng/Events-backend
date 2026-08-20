package com.events.modules.event.service;

import com.events.modules.event.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IEventService {
    UUID createEvent(CreateEventCommandDto command);
    List<String> uploadEventImages(UUID eventId, MultipartFile[] files) throws IOException;

    Page<GetEventDto> getAllEvents(int page, int size, String country);
    GetEventDto getEventById(UUID id);
    List<GetEventDto> getEventsNearby(Double lat, Double lon, Double radiusInMeters);
    List<GetEventDto> getAllIncomingEvents();
    /**
     * Search events based on dynamic criteria.
     *
     * @param criteria search criteria
     * @param pageable pagination and sorting parameters
     * @return paginated search results
     */
    Page<GetEventDto> getEvents(EventSearchCriteriaDto criteria, Pageable pageable);
    Boolean existById(UUID id);
    List<GetEventDto> getAllByIds(List<UUID> ids);

    void updateEvent(UUID id, UpdateEventCommandDto command);
    void generateSeatsForEvent(UUID id, List<GenerateSeatDto> seatNumbers);
    void softDeleteEvent(UUID eventId);
    void cancelEvent(UUID eventId);
    void publishEvent(UUID eventId);

    List<String> getDefaultPriceCategories();

    void saveAll(List<CreateEventCommandDto> events);
}

//TODO:void deleteEvent(Long id);
//TODO:List<EventDto> searchEvents(String keyword, String category, Double lat, Double lon, Double radiusInMeters);
//TODO:void addParticipant(Long eventId, Long userId);
//TODO:void removeParticipant(Long eventId, Long userId);
//TODO:List<EventDto> getParticipants(Long eventId);
//TODO:EventStatisticsDto getEventStatistics(Long eventId);