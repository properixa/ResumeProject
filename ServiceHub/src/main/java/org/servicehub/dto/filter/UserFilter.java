package org.servicehub.dto.filter;

import java.util.List;

public record UserFilter(
        String search,
        String phone,
        List<String> roles
) {
}