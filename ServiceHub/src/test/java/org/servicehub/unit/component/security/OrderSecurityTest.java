package org.servicehub.unit.component.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.component.security.order.OrderSecurity;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.repository.OrderRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderSecurityTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderSecurity orderSecurity;

    @Test
    void shouldAccessAdmin() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        var result = orderSecurity.canChange(1L, auth);
        assertThat(result).isTrue();
        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowWhenPrincipalIsNull() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Set.of()
                );

        assertThatThrownBy(() -> orderSecurity.canChange(1L, auth))
                .isInstanceOf(PrincipalException.class);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(1L, "test"),
                        null,
                        Set.of()
                );

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderSecurity.canChange(1L, auth))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldAccessExecutor() {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of()
                );
        var executor = new UserEntity();
        executor.setId(1L);
        var order = new OrderEntity();
        order.setExecutor(executor);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var result = orderSecurity.canChange(1L, auth);
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
        var executor = new UserEntity();
        executor.setId(1L);
        var order = new OrderEntity();
        order.setExecutor(executor);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var result = orderSecurity.canChange(1L, auth);
        assertThat(result).isFalse();
    }
}
