package com.events.modules.event.dto.mapper;

import com.events.modules.event.dto.CategoryCreateCommandDto;
import com.events.modules.event.dto.CategoryDto;
import com.events.modules.event.dto.CategoryUpdateCommandDto;
import com.events.modules.event.entity.aggregate.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ICategoryMapper {

    /**
     * Convert Category entity to CategoryDto.
     */
    CategoryDto toDto(Category category);

    /**
     * Convert list of Category entities to list of CategoryDtos.
     */
    List<CategoryDto> toDtoList(List<Category> categories);

    /**
     * Convert CategoryCreateCommandDto to Category entity.
     */
    Category toEntity(CategoryCreateCommandDto dto);

    /**
     * Convert CategoryDto to Category entity.
     */
    Category toEntity(CategoryDto dto);

    /**
     * Update existing Category entity from CategoryUpdateCommandDto.
     */
    void updateEntityFromDto(CategoryUpdateCommandDto dto, @MappingTarget Category category);
}
