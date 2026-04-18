package com.events.modules.event.dto.mapper;

import com.events.modules.event.dto.ImageDto;
import com.events.modules.event.entity.aggregate.Image;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IImageMapper {
        ImageDto toImageDto(Image image);

        List<ImageDto> toImageDto(List<Image> images);
}
