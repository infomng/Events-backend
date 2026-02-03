package com.events.modules.event.dto.mapper;


import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.dto.EventDto;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {IPriceCategoryMapper.class})
public interface IEventMapper {
    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "country.id", target = "countryId")
    @Mapping(source = "country.name", target = "countryName")
    @Mapping(source = "country.code", target = "countryCode")
    @Mapping(source = "priceCategories", target = "priceCategories")
    @Mapping(source = "country", target = "country")
    EventDto toDto(Event event);

    List<EventDto> toDtoList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "priceCategories", source = "categories")
    @Mapping(target = "status", constant = "DRAFT")
    Event toEntity(CreateEventCommandDto createEventCommandDto);
}