package org.servicehub.service;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.dto.order.OrderRequest;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.entity.enums.OrderStatus;
import org.servicehub.exception.exception.order.InvalidForServiceExecutorException;
import org.servicehub.exception.exception.order.OrderChangeStatusException;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.order.OrderStatusNotFoundException;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.OrderMapper;
import org.servicehub.repository.OrderRepository;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public OrderResponse create(OrderCreateRequest request,
                                UserPrincipal principal) {
        OrderEntity entity = mapper.toEntity(request);
        UserEntity customer = userRepository.findById(principal.id())
                .orElseThrow(() -> new UserNotFoundException("Customer user " + principal.id() + " not found"));

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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("@orderSecurity.canChange(#a0, authentication)")
    public void remove(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        orderRepository.delete(entity);
    }

    @Transactional
    @PreAuthorize("@orderSecurity.canChange(#a0, authentication)")
    public OrderResponse acceptOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        if (entity.getStatus().equals(OrderStatus.NEW)) {
            entity.setStatus(OrderStatus.ACCEPTED);
        } else {
            throw new OrderChangeStatusException("Order " + id + " unavailable to accept (wrong status)");
        }
        return mapper.toDto(entity);
    }

    @Transactional
    @PreAuthorize("@orderSecurity.canChange(#a0, authentication)")
    public OrderResponse startOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        if (entity.getStatus().equals(OrderStatus.ACCEPTED)) {
            entity.setStatus(OrderStatus.IN_PROGRESS);
        } else {
            throw new OrderChangeStatusException("Order " + id + " unavailable to start (wrong status)");
        }
        return mapper.toDto(entity);
    }

    @Transactional
    @PreAuthorize("@orderSecurity.canChange(#a0, authentication)")
    public OrderResponse cancelOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        if (!entity.getStatus().equals(OrderStatus.CANCELLED)) {
            entity.setStatus(OrderStatus.CANCELLED);
        } else {
            throw new OrderChangeStatusException("Order " + id + " unavailable to cancel (canceling cancel??)");
        }
        return mapper.toDto(entity);
    }

    @Transactional
    @PreAuthorize("@orderSecurity.canChange(#a0, authentication)")
    public OrderResponse completeOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
        if (entity.getStatus().equals(OrderStatus.IN_PROGRESS)) {
            entity.setStatus(OrderStatus.COMPLETED);
        } else {
            throw new OrderChangeStatusException("Order " + id + " unavailable to complete");
        }
        return mapper.toDto(entity);
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
