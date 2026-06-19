package org.servicehub.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull
        Long serviceId,
        @NotNull
        Long executorId,
        String details
) {
}
