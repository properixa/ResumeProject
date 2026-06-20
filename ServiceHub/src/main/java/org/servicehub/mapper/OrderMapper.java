package org.servicehub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.entity.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "executor", ignore = true)
    OrderEntity toEntity(OrderCreateRequest request);

    @Mapping(target = "executorId", source = "executor.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "serviceId", source = "service.id")
    OrderResponse toDto(OrderEntity entity);
}
