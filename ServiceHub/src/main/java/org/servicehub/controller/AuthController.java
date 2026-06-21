package org.servicehub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.servicehub.component.jwt.JwtTokenProvider;
import org.servicehub.dto.auth.LoginRequest;
import org.servicehub.dto.user.UserCreateRequest;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.service.ServicehubUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider provider;
    private final UserDetailsService userDetailsService;
    private final ServicehubUserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Set<String> roles = userDetailsService.loadUserByUsername(request.email())
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String token = provider.createToken(request.email(), roles);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + response.id()))
                .body(response);
    }
}
