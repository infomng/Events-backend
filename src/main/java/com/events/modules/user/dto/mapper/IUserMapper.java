package com.events.modules.user.dto.mapper;

import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface IUserMapper {

//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "fullName", target = "fullName")
//    @Mapping(source = "email", target = "email")
//    @Mapping(source = "verified", target = "isVerified")
    GetUserDto toGetUserDto(User user);
}
