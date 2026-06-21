package org.servicehub.component.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.security.PrincipalException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    public boolean canModifyAndDelete(Long id, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))) {
            return true;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if (principal == null) {
            throw new PrincipalException("User principal is null");
        }

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User " + id + " not found"));

        return user.getId().equals(principal.id());
    }

}
