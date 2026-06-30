package org.servicehub.unit.service;

import org.assertj.core.api.CollectionAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.ServiceMapper;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.servicehub.service.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceMapper mapper;

    @InjectMocks
    private ServiceService service;

    @Test
    void create_shouldCreateService() {
        var request = new ServiceCreateRequest("test",
                "test");
        var entity = new ServiceEntity();
        var executor = new UserEntity();

        when(mapper.toEntity(request))
                .thenReturn(entity);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));
        when(serviceRepository.save(entity))
                .thenReturn(entity);

        service.create(request, 1L);

        assertThat(entity.getExecutor()).isEqualTo(executor);
        verify(serviceRepository).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        var request = new ServiceCreateRequest("test", "test");
        var entity = new ServiceEntity();

        when(mapper.toEntity(request))
                .thenReturn(entity);
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request, 1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        var entity = new ServiceEntity();
        var dto = new ServiceResponse(1L, "test", "test", 1L, "full name");
        var pageable = Pageable.ofSize(20);

        when(serviceRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toDto(entity))
                .thenReturn(dto);

        var result = service.findAll(pageable);
        CollectionAssert.assertThatCollection(result.getContent()).hasSize(1);
    }

    @Test
    void findById_shouldReturn() {
        var entity = new ServiceEntity();
        var dto = new ServiceResponse(1L, "test", "test", 1L, "test test");

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(mapper.toDto(entity))
                .thenReturn(dto);

        assertThat(service.findById(1L)).isEqualTo(dto);
    }

    @Test
    void findById_shouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(UserServiceNotFoundException.class);
    }

    @Test
    void update_shouldUpdate() {
        var request = new ServiceUpdateRequest("test", "test", 1L);
        var entity = new ServiceEntity();
        var executor = new UserEntity();
        var role = new RoleEntity();
        role.setName("EXECUTOR");
        executor.setRoles(Set.of(role));
        var response = new ServiceResponse(1L, "test", "test", 1L, "test test");

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));
        when(mapper.toDto(entity))
                .thenReturn(response);

        var result = service.update(1L, request);
        assertThat(result).isEqualTo(response);
        assertThat(entity.getExecutor()).isEqualTo(executor);
    }

    @Test
    void update_shouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, new ServiceUpdateRequest("test", "test", 1L)))
                .isInstanceOf(UserServiceNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenExecutorNotFound() {
        var entity = new ServiceEntity();
        var request = new ServiceUpdateRequest("test", "test", 1L);

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenRoleIsInvalid() {
        var entity = new ServiceEntity();
        var request = new ServiceUpdateRequest("test", "test", 1L);
        var executor = new UserEntity();
        var role = new RoleEntity();
        role.setName("wrong");
        executor.setRoles(Set.of(role));

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    void remove_shouldRemove() {
        var entity = new ServiceEntity();

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.remove(1L);
        verify(serviceRepository).delete(entity);
    }

    @Test
    void remove_shouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(1L))
                .isInstanceOf(UserServiceNotFoundException.class);
    }

    @Test
    void findByExecutorId_shouldReturnServices() {
        ServiceEntity service1 = new ServiceEntity();
        ServiceEntity service2 = new ServiceEntity();
        service1.setId(1L);
        service2.setId(2L);

        when(serviceRepository.findAllByExecutorId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(service1, service2), Pageable.ofSize(20), 2));
        when(mapper.toDto(service1))
                .thenReturn(new ServiceResponse(1L, "", "", 1L, "full"));
        when(mapper.toDto(service2))
                .thenReturn(new ServiceResponse(2L, "", "", 2L, "name2"));

        var result = service.findAllByExecutorId(1L, Pageable.ofSize(20));
        assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
        assertThat(result.getContent().getLast().id()).isEqualTo(2L);
    }

    @Test
    void findByExecutorId_shouldReturnEmpty_whenNotFound() {
        when(serviceRepository.findAllByExecutorId(any(Long.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        var result = service.findAllByExecutorId(1L, Pageable.unpaged());
        CollectionAssert.assertThatCollection(result.getContent()).isEmpty();
    }
}
