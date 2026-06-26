package org.servicehub.unit.component.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.component.security.service.ServiceSecurity;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.repository.ServiceRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ServiceSecurityTest {
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    ServiceSecurity serviceSecurity;

    @Test
    void shouldAllowAdmin() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        var result = serviceSecurity.canModifyAndDelete(1L, auth);
        assertThat(result).isTrue();
        Mockito.verifyNoInteractions(serviceRepository);
    }

    @Test
    void shouldThrowWhenPrincipalIsNull() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Set.of()
                );

        assertThatThrownBy(() -> serviceSecurity.canModifyAndDelete(1L, auth))
                .isInstanceOf(PrincipalException.class);
    }

    @Test
    void shouldThrowWhenServiceNotFound() {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of()
                );

        Mockito.when(serviceRepository.findById(1L))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> serviceSecurity.canModifyAndDelete(1L, auth))
                .isInstanceOf(UserServiceNotFoundException.class);
    }

    @Test
    void shouldAccessOwner() {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of()
                );
        var owner = new UserEntity();
        owner.setId(1L);
        var entity = new ServiceEntity();
        entity.setExecutor(owner);

        Mockito.when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        var result = serviceSecurity.canModifyAndDelete(1L, auth);
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyOtherUser() {
        UserPrincipal principal = new UserPrincipal(2L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of()
                );
        var owner = new UserEntity();
        owner.setId(1L);
        var entity = new ServiceEntity();
        entity.setExecutor(owner);

        Mockito.when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        var result = serviceSecurity.canModifyAndDelete(1L, auth);
        assertThat(result).isFalse();
    }
}
