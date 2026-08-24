package com.events.modules.user.dto.mapper;

import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        uses = {ICountryMapper.class}
)
public interface IUserMapper {

    @Mapping(source = "verified", target = "isVerified")
    GetUserDto toGetUserDto(User user);
}
