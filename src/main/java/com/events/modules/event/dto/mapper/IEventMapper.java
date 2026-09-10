package com.events.modules.event.dto.mapper;


import com.events.common.config.Mapper.CentralMapperConfig;
import com.events.modules.category.dto.mapper.ICategoryMapper;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.dto.GetEventDto;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        config = CentralMapperConfig.class,
        uses = {IPriceCategoryMapper.class, IImageMapper.class, ICountryMapper.class, ICategoryMapper.class})
public interface IEventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(target = "isFreeEntry", ignore = true )
    GetEventDto toDto(Event event);

    List<GetEventDto> toDtoList(List<Event> events);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEvent(
            UpdateEventCommandDto command,
            @MappingTarget Event event
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "isTicketSalesActive", ignore = true)
    @Mapping(target = "availableTickets", ignore = true)
    @Mapping(target = "priceCategories", source = "priceCategories")
    Event toEntity(CreateEventCommandDto createEventCommandDto);
}