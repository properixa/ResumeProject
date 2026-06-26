package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.LoginRequest;
import org.servicehub.dto.user.UserCreateRequest;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.service.AuthService;
import org.servicehub.validation.groups.ValidationSequence;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.getToken(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Validated(ValidationSequence.class) @RequestBody UserCreateRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + response.id()))
                .body(response);
    }
}
