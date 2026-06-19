package org.servicehub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.servicehub.dto.UserCreateRequest;
import org.servicehub.dto.UserResponse;
import org.servicehub.dto.UserUpdateRequest;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.DuplicateEmailException;
import org.servicehub.exception.exception.UserNotFoundException;
import org.servicehub.mapper.UserMapper;
import org.servicehub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicehubUserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public UserResponse create(UserCreateRequest data) {
        if (repository.existsByEmail(data.email())) {
            throw new DuplicateEmailException("Email " + data.email() + " already exists");
        }
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
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity userEntity = repository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User %d not found".formatted(id)));

        if (!userEntity.getEmail().equals(request.email()) &&
                repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email " + request.email() + " already exists");
        }

        mapper.toEntity(request, userEntity);

        return mapper.toDto(userEntity);
    }

    @Transactional
    public void remove(Long id) {
        repository.deleteById(id);
    }
}
