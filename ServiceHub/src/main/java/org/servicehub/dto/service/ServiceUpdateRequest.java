package org.servicehub.dto.service;

import jakarta.validation.constraints.NotBlank;

public record ServiceUpdateRequest(
        @NotBlank(message = "title must be filled")
        String title,
        String description,
        Long executorId
) {
}
