package org.servicehub.component.security.order;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.OrderEntity;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderSecurity {
    private final OrderRepository orderRepository;

    @Transactional(
            propagation = Propagation.REQUIRED
    )
    public boolean canChange(Long id, Authentication auth) {
        if (auth.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))) {
            return true;
        }

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        if (principal == null) {
            throw new PrincipalException("Principal is null");
        }
        return order.getExecutor().getId().equals(principal.id());
    }
}
