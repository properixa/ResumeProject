package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.UserCreateRequest;
import org.servicehub.dto.UserResponse;
import org.servicehub.service.ServicehubUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ServicehubUserService servicehubUserService;

    @GetMapping
    public List<UserResponse> getAll() {
        return servicehubUserService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable("id") Long id) {
        return servicehubUserService.getById(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        UserResponse user = servicehubUserService.create(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(user);
    }
}
