package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.filter.UserFilter;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.dto.user.UserUpdateRequest;
import org.servicehub.service.ServicehubUserService;
import org.servicehub.validation.groups.ValidationSequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ServicehubUserService servicehubUserService;

    @GetMapping
    public Page<UserResponse> getAll(@ModelAttribute UserFilter filter,
                                     @PageableDefault Pageable page) {
        return servicehubUserService.getAll(filter, page);
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
