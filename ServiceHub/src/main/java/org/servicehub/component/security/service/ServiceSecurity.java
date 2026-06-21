package org.servicehub.component.security.service;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.repository.ServiceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ServiceSecurity {

    private final ServiceRepository serviceRepository;

    public boolean canModifyAndDelete(Long id, Authentication auth) {

        if (auth.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))) {
            return true;
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        if (principal == null) {
            throw new PrincipalException("User principal is null");
        }
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        return service.getExecutor().getId().equals(principal.id());
    }

}
