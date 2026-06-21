package org.servicehub.service;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.user.UserCreateRequest;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.dto.user.UserUpdateRequest;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.user.DuplicateEmailException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.UserMapper;
import org.servicehub.repository.RoleRepository;
import org.servicehub.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServicehubUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserCreateRequest data) {
        if (userRepository.existsByEmail(data.email())) {
            throw new DuplicateEmailException("Email " + data.email() + " already exists");
        }
        UserEntity user = mapper.toEntity(data);
        RoleEntity role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.getRoles().add(role);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(data.password()));

        return mapper.toDto(userRepository.save(user));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id).map(mapper::toDto).orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    @Transactional
    @PreAuthorize("@userSecurity.canModifyAndDelete(#a0, authentication)")
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User %d not found".formatted(id)));

        if (!userEntity.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email " + request.email() + " already exists");
        }

        if (request.roles() != null) {
            Set<RoleEntity> newRoles = new HashSet<>();
            for (String roleName : request.roles()) {
                RoleEntity role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InvalidRoleException("Role " + roleName + " doesn't exist"));
                newRoles.add(role);
            }
            userEntity.getRoles().clear();
            userEntity.getRoles().addAll(newRoles);
        }

        mapper.toEntity(request, userEntity);

        return mapper.toDto(userEntity);
    }

    @Transactional
    @PreAuthorize("@userSecurity.canModifyAndDelete(#a0, authentication)")
    public void remove(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User " + id + " not found"));
        userRepository.delete(entity);
    }
}
