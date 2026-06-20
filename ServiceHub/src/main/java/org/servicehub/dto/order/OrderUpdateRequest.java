package org.servicehub.dto.order;

import jakarta.validation.constraints.NotNull;

public record OrderUpdateRequest(
        @NotNull(message = "Executor id must be set")
        Long executorId,
        @NotNull(message = "Service id must be set")
        Long serviceId,
        String details,
        String status
) implements OrderRequest {
}
