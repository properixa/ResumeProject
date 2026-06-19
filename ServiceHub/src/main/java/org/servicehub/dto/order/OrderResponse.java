package org.servicehub.dto.order;

public record OrderResponse(
        Long id,
        Long customerId,
        Long executorId,
        Long serviceId,
        String details,
        String status
) {
}
