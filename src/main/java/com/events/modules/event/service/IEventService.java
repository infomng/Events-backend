package com.events.modules.event.service;

import com.events.common.dto.pagination.PageResponseDto;
import com.events.modules.event.dto.*;
import com.events.modules.user.dto.GetUserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IEventService {
    UUID createEvent(CreateEventCommandDto command);
    List<String> uploadEventImages(UUID eventId, MultipartFile[] files) throws IOException;

    PageResponseDto<GetEventDto> getAllEvents(int page, int size, String country);
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
    PageResponseDto<GetEventDto> getEvents(EventSearchCriteriaDto criteria, Pageable pageable);
    Boolean existById(UUID id);
    List<GetEventDto> getAllByIds(List<UUID> ids);

    GetEventDto updateEvent(UUID id, UpdateEventCommandDto command);
    void generateSeatsForEvent(UUID id, List<GenerateSeatDto> seatNumbers);
    void softDeleteEvent(UUID eventId);
    void cancelEvent(UUID eventId);
    void publishEvent(UUID eventId);

    List<String> getDefaultPriceCategories();

    void saveAll(List<CreateEventCommandDto> events);

    /**
     * Bloque des billets d'une catégorie de prix, de façon atomique.
     * @throws com.events.modules.event.exception.TicketsSoldOutException s'il ne reste pas assez de places
     */
    void reserveTickets(UUID eventId, UUID priceCategoryId, int quantity);

    /**
     * Libère des billets précédemment bloqués (réservation expirée, annulée ou remboursée).
     */
    void releaseTickets(UUID eventId, UUID priceCategoryId, int quantity);

    /**
     * L'utilisateur courant peut-il contrôler les entrées de l'événement ?
     * (organisateur, membre du staff de l'événement ou administrateur)
     */
    boolean isCurrentUserEntryManager(UUID eventId);

    /** Ajoute un membre du staff (réservé à l'organisateur ou à un administrateur). */
    void addStaff(UUID eventId, AddEventStaffDto dto);

    /** Retire un membre du staff (réservé à l'organisateur ou à un administrateur). */
    void removeStaff(UUID eventId, UUID userId);

    List<GetUserDto> getStaff(UUID eventId);
}

//TODO:void deleteEvent(Long id);
//TODO:List<EventDto> searchEvents(String keyword, String category, Double lat, Double lon, Double radiusInMeters);
//TODO:void addParticipant(Long eventId, Long userId);
//TODO:void removeParticipant(Long eventId, Long userId);
//TODO:List<EventDto> getParticipants(Long eventId);
//TODO:EventStatisticsDto getEventStatistics(Long eventId);