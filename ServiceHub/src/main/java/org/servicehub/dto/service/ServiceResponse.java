package org.servicehub.dto.service;

public record ServiceResponse(
        Long id,
        String title,
        String description,
        Long executorId,
        String executorFullName
) {
}
