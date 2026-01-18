package com.events.modules.event.dto.mapper;


import com.events.modules.event.entity.Event;
import com.events.modules.event.dto.EventDto;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IEventMapper {
    @Mapping(source = "organizer.id", target = "organizerId")
    EventDto toDto(Event event);
    List<EventDto> toDtoList(List<Event> events);
}