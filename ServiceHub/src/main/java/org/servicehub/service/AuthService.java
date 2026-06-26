package org.servicehub.service;

import lombok.RequiredArgsConstructor;
import org.servicehub.component.jwt.JwtTokenProvider;
import org.servicehub.component.security.ServicehubUserDetails;
import org.servicehub.dto.auth.LoginRequest;
import org.servicehub.dto.user.UserCreateRequest;
import org.servicehub.dto.user.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider provider;
    private final ServicehubUserService userService;

    public String getToken(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        ServicehubUserDetails principal =
                (ServicehubUserDetails) auth.getPrincipal();

        assert principal != null;
        return provider.createToken(principal);
    }

    public UserResponse register(UserCreateRequest request) {
        return userService.create(request);
    }
}
