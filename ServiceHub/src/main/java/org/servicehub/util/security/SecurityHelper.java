package org.servicehub.util.security;

import org.springframework.security.core.Authentication;

import java.util.Objects;

public class SecurityHelper {
    public static boolean isAdmin(Authentication auth) {
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"));
        }
        return false;
    }
}
