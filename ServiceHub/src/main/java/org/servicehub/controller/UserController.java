package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.dto.user.UserUpdateRequest;
import org.servicehub.service.ServicehubUserService;
import org.servicehub.validation.groups.ValidationSequence;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long id,
            @Validated(ValidationSequence.class) @RequestBody UserUpdateRequest request) {

        UserResponse user = servicehubUserService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id") Long id
    ) {
        servicehubUserService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
