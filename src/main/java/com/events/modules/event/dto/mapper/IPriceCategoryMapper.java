package com.events.modules.event.dto.mapper;

import com.events.modules.event.dto.PriceCategoryDto;
import com.events.modules.event.entity.aggregate.PriceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IPriceCategoryMapper {

    PriceCategoryDto toDto(PriceCategory priceCategory);

    List<PriceCategoryDto> toDtoList(List<PriceCategory> priceCategories);

    @Mapping(target = "event", ignore = true)
    @Mapping(target = "availableTickets", source = "totalTickets")
    PriceCategory toEntity(PriceCategoryDto priceCategoryDto);

    List<PriceCategory> toEntityList(List<PriceCategoryDto> priceCategoryDtos);
}
