package org.servicehub.unit;

import org.assertj.core.api.CollectionAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.entity.enums.OrderStatus;
import org.servicehub.exception.exception.order.InvalidForServiceExecutorException;
import org.servicehub.exception.exception.order.OrderChangeStatusException;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.order.OrderStatusNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.mapper.OrderMapper;
import org.servicehub.repository.OrderRepository;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.servicehub.service.OrderService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderService service;

    @Test
    void create_shouldCreate() {
        var request = new OrderCreateRequest(1L, 1L, "");
        var principal = new UserPrincipal(2L, "test");
        var entity = new OrderEntity();
        var customer = new UserEntity();
        customer.setId(2L);
        var executor = new UserEntity();
        executor.setId(1L);
        var executorRole = new RoleEntity();
        executorRole.setName("EXECUTOR");
        executor.setRoles(Set.of(executorRole));
        var orderService = new ServiceEntity();
        orderService.setExecutor(executor);
        orderService.setId(1L);

        when(mapper.toEntity(request))
                .thenReturn(entity);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(customer));
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(orderService));


        service.create(request, principal);
        verify(orderRepository).save(entity);
        verify(mapper).toDto(entity);
        assertThat(entity.getCustomer()).isEqualTo(customer);
        assertThat(entity.getExecutor()).isEqualTo(executor);
        assertThat(entity.getService()).isEqualTo(orderService);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void create_shouldThrowWhenCustomerNotFound() {
        var request = new OrderCreateRequest(1L, 1L, "test");
        var principal = new UserPrincipal(1L, "test");

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request, principal))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findById_shouldReturn() {
        var response = new OrderResponse(1L, 1L, 1L, 1L, "test", "test");
        var entity = new OrderEntity();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(mapper.toDto(entity))
                .thenReturn(response);

        var result = service.findById(1L);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void findAll_shouldReturn() {
        var entity = new OrderEntity();

        when(orderRepository.findAll())
                .thenReturn(List.of(entity));
        when(mapper.toDto(entity))
                .thenReturn(new OrderResponse(1L, 1L, 1L, 1L, "test", "test"));

        var result = service.findAll();
        CollectionAssert.assertThatCollection(result).hasSize(1);
    }

    @Test
    void update_shouldUpdate() {
        var entity = new OrderEntity();
        var request = new OrderUpdateRequest(1L, 1L, "test", "NEW");
        var executor = new UserEntity();
        executor.setId(1L);
        var executorRole = new RoleEntity();
        executorRole.setName("EXECUTOR");
        executor.setRoles(Set.of(executorRole));
        var orderService = new ServiceEntity();
        orderService.setExecutor(executor);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(orderService));

        service.update(1L, request);
        assertThat(entity.getService()).isEqualTo(orderService);
        assertThat(entity.getExecutor()).isEqualTo(executor);
        verify(mapper).toDto(entity);
    }

    @Test
    void update_shouldThrowWhenOrderNotFound() {
        var request = new OrderUpdateRequest(1L, 1L, "test", "test");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenExecutorIdIsNull() {
        var request = new OrderUpdateRequest(null, 1L, "test", "test");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(new OrderEntity()));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_shouldThrowWhenServiceIdIsNull() {
        var request = new OrderUpdateRequest(1L, null, "test", "test");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(new OrderEntity()));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_shouldThrowWhenStatusIsInvalid() {
        var request = new OrderUpdateRequest(1L, 1L, "test", "test");
        var executor = new UserEntity();
        executor.setId(1L);
        var executorRole = new RoleEntity();
        executorRole.setName("EXECUTOR");
        executor.setRoles(Set.of(executorRole));
        var orderService = new ServiceEntity();
        orderService.setExecutor(executor);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(new OrderEntity()));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(executor));
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(orderService));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(OrderStatusNotFoundException.class);
    }

    @Test
    void remove_shouldRemove() {
        var entity = new OrderEntity();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.remove(1L);
        verify(orderRepository).delete(entity);
    }

    @Test
    void remove_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void acceptOrder_shouldAccept() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.acceptOrder(1L);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        verify(mapper).toDto(entity);
    }

    @Test
    void acceptOrder_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void acceptOrder_shouldThrowWhenStatusIncorrect() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.acceptOrder(1L))
                .isInstanceOf(OrderChangeStatusException.class);
    }

    @Test
    void startOrder_shouldStart() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.ACCEPTED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.startOrder(1L);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        verify(mapper).toDto(entity);
    }

    @Test
    void startOrder_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void startOrder_shouldThrowWhenStatusIncorrect() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.startOrder(1L))
                .isInstanceOf(OrderChangeStatusException.class);
    }

    @Test
    void cancelOrder_shouldCancel() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.cancelOrder(1L);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(mapper).toDto(entity);
    }

    @Test
    void cancelOrder_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void cancelOrder_shouldThrowWhenStatusIncorrect() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(OrderChangeStatusException.class);
    }

    @Test
    void completeOrder_shouldComplete() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        service.completeOrder(1L);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(mapper).toDto(entity);
    }

    @Test
    void completeOrder_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void completeOrder_shouldThrowWhenStatusIncorrect() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.completeOrder(1L))
                .isInstanceOf(OrderChangeStatusException.class);
    }

    @Test
    void validateAndSetExecutorAndService_shouldThrowWhenExecutorForServiceInvalid() {
        var entity = new OrderEntity();
        var request = new OrderCreateRequest(1L, 3L, "test");
        var principal = new UserPrincipal(1L, "test");
        var customer = new UserEntity();
        var executor = new UserEntity();
        executor.setId(3L);
        var serviceExecutor = new UserEntity();
        serviceExecutor.setId(2L);
        var orderService = new ServiceEntity();
        orderService.setExecutor(serviceExecutor);

        when(mapper.toEntity(request))
                .thenReturn(entity);
        when(userRepository.findById(principal.id()))
                .thenReturn(Optional.of(customer));
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(executor));
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(orderService));

        assertThatThrownBy(() -> service.create(request, principal))
                .isInstanceOf(InvalidForServiceExecutorException.class);
    }

    @Test
    void validateAndSetExecutorAndService_shouldThrowWhenExecutorRoleIncorrect() {
                var entity = new OrderEntity();
        var request = new OrderCreateRequest(1L, 2L, "test");
        var principal = new UserPrincipal(1L, "test");
        var customer = new UserEntity();
        var serviceExecutor = new UserEntity();
        serviceExecutor.setId(2L);
        var orderService = new ServiceEntity();
        orderService.setExecutor(serviceExecutor);

        when(mapper.toEntity(request))
                .thenReturn(entity);
        when(userRepository.findById(principal.id()))
                .thenReturn(Optional.of(customer));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(serviceExecutor));
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(orderService));

        assertThatThrownBy(() -> service.create(request, principal))
                .isInstanceOf(InvalidRoleException.class);
    }
}
