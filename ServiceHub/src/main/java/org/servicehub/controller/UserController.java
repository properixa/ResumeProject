package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.PostUser;
import org.servicehub.entity.ServicehubUser;
import org.servicehub.service.ServicehubUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ServicehubUserService servicehubUserService;

    @GetMapping
    public Iterable<ServicehubUser> getAll() {
        return servicehubUserService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<ServicehubUser> getUser(@PathVariable Long id) {
        return servicehubUserService.getById(id);
    }

    @PostMapping
    public ServicehubUser createUser(@RequestBody PostUser postUser) {
        ServicehubUser user = new ServicehubUser();
        user.setEmail(postUser.email());
        user.setFullName(postUser.fullName());
        user.setPhone(postUser.phone());
        servicehubUserService.create(user);
        return user;
    }
}
