package org.servicehub.dto.service;


import jakarta.validation.constraints.NotBlank;

public record ServiceCreateRequest(
        @NotBlank(message = "title should be filled")
        String title,
        String description
) {
}
