package org.servicehub.dto.auth;

import lombok.Getter;

public record UserPrincipal(
        @Getter Long id,
        @Getter String email
) {}
