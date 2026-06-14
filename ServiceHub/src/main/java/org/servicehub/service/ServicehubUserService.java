package org.servicehub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.servicehub.entity.ServicehubUser;
import org.servicehub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicehubUserService {
    private final UserRepository repository;

    @Transactional
    public void create(ServicehubUser user) {
        repository.save(user);
    }

    public Iterable<ServicehubUser> getAll() {
        return repository.findAll();
    }

    public Optional<ServicehubUser> getById(Long id) {
        return repository.findById(id);
    }

    public void remove(Long id) {
        repository.deleteById(id);
    }
}
