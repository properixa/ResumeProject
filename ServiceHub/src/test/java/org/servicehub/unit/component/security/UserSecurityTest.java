package org.servicehub.unit.component.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.component.security.user.UserSecurity;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSecurityTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSecurity userSecurity;

    @Test
    void shouldAllowedAdmin() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        boolean result = userSecurity.canModifyAndDelete(1L, auth);

        assertThat(result).isTrue();

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenPrincipalIsNull() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Set.of()
                );

        assertThatThrownBy(() -> userSecurity.canModifyAndDelete(1L, auth))
                .isInstanceOf(PrincipalException.class);
    }

    @Test
    void shouldAllowOwner() {
        UserPrincipal principal = new UserPrincipal(1L, "email");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        var user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        var result = userSecurity.canModifyAndDelete(1L, auth);
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyOtherUser() {
        UserPrincipal principal = new UserPrincipal(2L, "email");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Set.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        var user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        var result = userSecurity.canModifyAndDelete(1L, auth);
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSecurity.canModifyAndDelete(1L, auth))
                .isInstanceOf(UserNotFoundException.class);
    }
}
