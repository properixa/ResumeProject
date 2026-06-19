package org.servicehub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "executor", ignore = true)
    ServiceEntity toEntity(ServiceCreateRequest request);

    @Mapping(target = "executorId", source = "executor.id")
    @Mapping(target = "executorFullName", source = ".", qualifiedByName = "toExecutorFullName")
    ServiceResponse toDto(ServiceEntity entity);

    @Named("toExecutorFullName")
    default String toExecutorFullName(ServiceEntity entity) {
        UserEntity executor = entity.getExecutor();
        if (executor == null) return null;
        return Stream.of(executor.getSurname(), executor.getName(), executor.getPatronymic())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "executor", ignore = true)
    void toEntity(ServiceUpdateRequest request, @MappingTarget ServiceEntity entity);
}
