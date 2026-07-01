package org.servicehub.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.repository.UserRepository;
import org.servicehub.service.ServicehubUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ServicehubUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServicehubUserDetailsService userDetailsService;

    @Test
    void loadByUsername_shouldReturnUserDetails_whenAllFine() {
        var user = new UserEntity();
        user.setEmail("email");
        user.setPassword("password");
        user.setEnabled(true);
        var role = new RoleEntity();
        role.setName("Role");
        user.setRoles(Set.of(role));

        Mockito.when(userRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.of(user));

        var result = userDetailsService.loadUserByUsername("email");
        assertThatCollection(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().stream()
                .anyMatch(x -> Objects.equals(x.getAuthority(), "ROLE_Role"))).isTrue();
        assertThat(result.getUsername()).isEqualTo("email");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadByUsername_shouldThrow_whenUserNotFound() {
        Mockito.when(userRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("any"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
