package org.servicehub.service;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.dto.order.OrderRequest;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.entity.enums.OrderStatus;
import org.servicehub.exception.exception.order.InvalidForServiceExecutorException;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.order.OrderStatusNotFoundException;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.OrderMapper;
import org.servicehub.repository.OrderRepository;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final OrderMapper mapper;

    @Transactional
    public OrderResponse create(OrderCreateRequest request, Long customerId) {
        OrderEntity entity = mapper.toEntity(request);
        UserEntity customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer user " + customerId + " not found"));

        validateAndSetExecutorAndService(request, entity);

        entity.setCustomer(customer);
        entity.setStatus(OrderStatus.NEW);
        orderRepository.save(entity);
        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return mapper.toDto(orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found")));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public OrderResponse update(Long id, OrderUpdateRequest request) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));

        if (request.executorId() != null && request.serviceId() != null) {
            validateAndSetExecutorAndService(request, entity);
        } else if (request.executorId() != null || request.serviceId() != null) {
            throw new IllegalArgumentException("Both executorId and serviceId must be provided together");
        }

        if (request.status() != null) {
            entity.setStatus(
                    OrderStatus.fromString(request.status())
                            .orElseThrow(() ->
                                    new OrderStatusNotFoundException("Invalid order status: " + request.status()))
            );
        }

        if (request.details() != null) {
            entity.setDetails(request.details());
        }

        return mapper.toDto(entity);
    }

    @Transactional
    public void remove(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

    private void validateAndSetExecutorAndService(OrderRequest request, OrderEntity entity) {
        UserEntity executor = userRepository.findById(request.executorId())
                .orElseThrow(() -> new UserNotFoundException("Executor user " + request.executorId() + " not found"));
        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new UserServiceNotFoundException("Service " + request.serviceId() + " not found"));

        if (!executor.getId().equals(service.getExecutor().getId())) {
            throw new InvalidForServiceExecutorException("User " + executor.getId() + " invalid executor for service "
                    + service.getId());
        }

        if (executor.getRoles().stream().noneMatch(x -> x.getName().equals("EXECUTOR"))) {
            throw new InvalidRoleException("User is not executor");
        }

        entity.setExecutor(executor);
        entity.setService(service);
    }
}
