package org.servicehub.component.security;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class ServicehubUserDetails extends User {

    @Getter
    private final Long id;

    public ServicehubUserDetails(Long id,
                                 String username,
                                 @Nullable String password,
                                 boolean enabled,
                                 boolean accountNonExpired,
                                 boolean credentialsNonExpired,
                                 boolean accountNotLocked,
                                 Collection<? extends GrantedAuthority> authorities
                                 ) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNotLocked, authorities);
        this.id = id;
    }
}
