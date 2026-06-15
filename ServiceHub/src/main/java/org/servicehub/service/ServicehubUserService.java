package org.servicehub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.servicehub.dto.UserCreateRequest;
import org.servicehub.dto.UserResponse;
import org.servicehub.entity.UserEntity;
import org.servicehub.exceptions.UserNotFoundException;
import org.servicehub.mapper.UserMapper;
import org.servicehub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicehubUserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public UserResponse create(UserCreateRequest data) {
        UserEntity user = mapper.toEntity(data);

        return mapper.toDto(repository.save(user));
    }

    public List<UserResponse> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public UserResponse getById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    @Transactional
    public void remove(Long id) {
        repository.deleteById(id);
    }
}
