package com.events.modules.event.dto.mapper;


import com.events.modules.category.dto.mapper.ICategoryMapper;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.dto.GetEventDto;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {IPriceCategoryMapper.class, IImageMapper.class, ICountryMapper.class, ICategoryMapper.class})
public interface IEventMapper {
    @Mapping(source = "organizer.id", target = "organizerId")
    GetEventDto toDto(Event event);

    List<GetEventDto> toDtoList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "priceCategories", source = "priceCategories")
    @Mapping(target = "status", constant = "DRAFT")
    Event toEntity(CreateEventCommandDto createEventCommandDto);
}