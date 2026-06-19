package org.servicehub.mapper;

import org.mapstruct.*;
import org.servicehub.dto.UserCreateRequest;
import org.servicehub.dto.UserResponse;
import org.servicehub.dto.UserUpdateRequest;
import org.servicehub.entity.UserEntity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", source = ".", qualifiedByName = "toFullName")
    UserResponse toDto(UserEntity entity);

    @Named("toFullName")
    default String toFullName(UserEntity entity) {
        return Stream.of(entity.getSurname(), entity.getName(), entity.getPatronymic())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "surname", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "patronymic", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(UserCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "surname", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "patronymic", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void toEntity(UserUpdateRequest dto, @MappingTarget UserEntity entity);

    @AfterMapping
    default void parseFullName(Object dto, @MappingTarget UserEntity entity) {
        String fullName = null;
        if (dto instanceof UserCreateRequest create) {
            fullName = create.fullName();
        } else if (dto instanceof UserUpdateRequest update) {
            fullName = update.fullName();
        }
        if (fullName != null) {
            String[] parts = fullName.strip().split("\\s+");
            entity.setSurname(parts.length > 0 ? parts[0] : null);
            entity.setName(parts.length > 1 ? parts[1] : null);
            entity.setPatronymic(parts.length > 2 ? parts[2] : null);
        }
    }

    @AfterMapping
    default void parseRoles(Object dto, @MappingTarget UserEntity entity) {

        if (dto instanceof UserUpdateRequest) {

        }
    }
}