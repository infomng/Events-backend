package com.events.modules.user.dto.mapper;

import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface IUserMapper {

    @Mapping(source = "verified", target = "isVerified")
    @Mapping(source = "country.id", target = "countryId")
    @Mapping(source = "country.name", target = "countryName")
    @Mapping(source = "country.code", target = "countryCode")
    GetUserDto toGetUserDto(User user);
}
