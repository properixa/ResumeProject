package org.servicehub.service;


import lombok.RequiredArgsConstructor;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.ServiceMapper;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServicehubUserService userService;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceMapper mapper;

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request, Long executorId) {

        ServiceEntity entity = mapper.toEntity(request);

        UserEntity executor = userRepository.findById(executorId)
                .orElseThrow(() -> new UserNotFoundException("User " + executorId + " not found"));

        if (executor.getRoles().stream().noneMatch(x -> x.getName().equals("EXECUTOR"))) {
            throw new InvalidRoleException("User is not executor");
        }

        entity.setExecutor(executor);

        return mapper.toDto(serviceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> findAll() {
        return serviceRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return mapper.toDto(serviceRepository.findById(id)
                .orElseThrow(() -> new UserServiceNotFoundException("Service " + id + " not found")));
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceUpdateRequest request) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new UserServiceNotFoundException("Service " + id + " not found"));

        mapper.toEntity(request, entity);
        UserEntity newExecutor = userRepository.findById(request.executorId())
                .orElseThrow(() -> new UserNotFoundException("User " + request.executorId() + " not found"));

        if (newExecutor.getRoles().stream().noneMatch(r -> r.getName().equals("EXECUTOR"))) {
            throw new InvalidRoleException("User is not executor");
        }
        entity.setExecutor(newExecutor);

        return mapper.toDto(entity);
    }

    @Transactional
    public void remove(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new UserServiceNotFoundException("Service " + id + " not found");
        }
        serviceRepository.deleteById(id);
    }
}
