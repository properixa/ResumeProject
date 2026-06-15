package org.servicehub.mapper;

import org.mapstruct.Mapper;
import org.servicehub.dto.UserCreateRequest;
import org.servicehub.dto.UserResponse;
import org.servicehub.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(UserEntity entity);
    UserEntity toEntity(UserCreateRequest dto);
}
