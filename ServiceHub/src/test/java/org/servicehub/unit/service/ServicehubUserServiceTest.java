package org.servicehub.unit.service;

import org.assertj.core.api.CollectionAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.dto.filter.UserFilter;
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
import org.servicehub.repository.specification.UserSpecification;
import org.servicehub.service.ServicehubUserService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicehubUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ServicehubUserService service;

    @Test
    void create_shouldCreateUser() {
        var request = new UserCreateRequest("name name name",
                "password",
                "email@example.com",
                "number");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        var entity = new UserEntity();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(new UserResponse(1L,
                "name name name",
                "email@example.com",
                "number"));

        var result = service.create(request);

        assertThat(result.email()).isEqualTo("email@example.com");

        verify(userRepository).save(entity);
    }

    @Test
    void create_shouldThrowWhenEmailExists() {
        var request = new UserCreateRequest("name name name",
                "password",
                "email@example.com",
                "number");

        when(userRepository.existsByEmail("email@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAll_shouldReturnUsers() {
        var entity = new UserEntity();
        var dto = new UserResponse(1L, "test", "test", "test");
        var pageable = Pageable.ofSize(20);
        var userFilter = new UserFilter(null, null, null);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(entity), Pageable.ofSize(20), 1));
        when(mapper.toDto(entity)).thenReturn(dto);

        var result = service.getAll(userFilter, pageable).toList();
        CollectionAssert.assertThatCollection(result).hasSize(1);
    }

    @Test
    void getById_shouldReturn() {
        var entity = new UserEntity();
        var dto = new UserResponse(1L, "test", "testEmail", "test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        var result = service.getById(1L);
        assertThat(result.email()).isEqualTo("testEmail");
    }

    @Test
    void getById_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_shouldUpdateUser() {
        var request = new UserUpdateRequest("name",
                "password",
                "email@email",
                "num",
                null);
        var entity = new UserEntity();
        entity.setName("oldName");
        entity.setPassword("oldPassword");
        entity.setEmail("oldEmail");
        entity.setPhone("oldPhone");
        var response = new UserResponse(
                1L,
                "name",
                "email@email",
                "num"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(mapper.toDto(entity)).thenReturn(response);

        service.update(1L, request);
        verify(mapper).toEntity(request, entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        var request = new UserUpdateRequest("test",
                "test",
                "test",
                "test",
                null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenEmailExists() {
        var entity = new UserEntity();
        entity.setEmail("email");
        var request = new UserUpdateRequest("test",
                "test",
                "exists",
                "test",
                null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void update_shouldThrowWhenRoleNotExists() {
        var entity = new UserEntity();
        entity.setEmail("email");
        var request = new UserUpdateRequest("test",
                "test",
                "test",
                "test",
                Set.of("unknown"));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);
        when(roleRepository.findByName("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    void update_shouldUpdateWithRoles() {
        var entity = new UserEntity();
        entity.setEmail("email");
        var request = new UserUpdateRequest("test",
                "test",
                "test",
                "test",
                Set.of("test"));
        var role = new RoleEntity();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);
        when(roleRepository.findByName("test"))
                .thenReturn(Optional.of(role));

        service.update(1L, request);
        verify(mapper).toEntity(request, entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void update_shouldNotCheckEmail() {
        var entity = new UserEntity();
        entity.setEmail("email");
        var request = new UserUpdateRequest("test",
                "test",
                "email",
                "test",
                null);
        var response = new UserResponse(1L,
                "test",
                "email",
                "test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(response);

        service.update(1L, request);
        verify(userRepository, never()).existsByEmail(entity.getEmail());
    }

    @Test
    void remove_shouldDelete() {
        var entity = new UserEntity();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.remove(1L);
        verify(userRepository).delete(entity);
    }

    @Test
    void remove_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(1L))
                .isInstanceOf(UserNotFoundException.class);
    }
}


