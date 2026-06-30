package org.servicehub.dto.filter;

import org.servicehub.entity.enums.OrderStatus;

public record OrderFilter(Long executorId, OrderStatus status) {
}
