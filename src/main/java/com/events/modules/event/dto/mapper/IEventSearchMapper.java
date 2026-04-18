package com.events.modules.event.dto.mapper;

import com.events.modules.event.dto.EventSearchViewDto;
import com.events.modules.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Event search projections.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IEventSearchMapper {

    @Mapping(source = "category.id", target = "category.id")
    @Mapping(source = "category.name", target = "category.name")
    @Mapping(source = "country.id", target = "country.id")
    @Mapping(source = "country.name", target = "country.name")
    @Mapping(source = "country.code", target = "country.code")
    EventSearchViewDto toSearchView(Event event);

    List<EventSearchViewDto> toSearchViewList(List<Event> events);
}
