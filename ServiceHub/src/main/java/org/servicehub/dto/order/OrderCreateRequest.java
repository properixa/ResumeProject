package org.servicehub.dto.order;

import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull(message = "Service id must be set")
        Long serviceId,
        @NotNull(message = "Executor id must be set")
        Long executorId,
        String details
) implements OrderRequest {
}
