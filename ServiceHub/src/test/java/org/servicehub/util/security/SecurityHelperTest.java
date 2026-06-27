package org.servicehub.util.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SecurityHelperTest {

    @Test
    void isAdmin_shouldReturnTrue_whenAuthorityContainsAdmin() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        assertThat(SecurityHelper.isAdmin(auth)).isTrue();
    }

    @Test
    void isAdmin_shouldReturnFalse_whenAuthorityNotAdmin() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_TEST"))
                );

        assertThat(SecurityHelper.isAdmin(auth)).isFalse();
    }

    @Test
    void isAdmin_shouldReturnFalse_whenAuthenticationIsNull() {
        assertThat(SecurityHelper.isAdmin(null)).isFalse();
    }
}
