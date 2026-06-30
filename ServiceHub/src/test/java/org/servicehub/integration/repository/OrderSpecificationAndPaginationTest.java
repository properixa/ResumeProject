package org.servicehub.integration.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.servicehub.config.PersistenceTestConfig;
import org.servicehub.dto.filter.OrderFilter;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.entity.enums.OrderStatus;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.repository.OrderRepository;
import org.servicehub.repository.RoleRepository;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.servicehub.repository.specification.OrderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@Transactional
class OrderSpecificationAndPaginationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE orders, users, services RESTART IDENTITY CASCADE").executeUpdate();
        RoleEntity roleExecutor = roleRepository.findByName("EXECUTOR")
                .orElseThrow(() -> new InvalidRoleException("Role name invalid"));
        RoleEntity roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new InvalidRoleException("Role name invalid"));

        UserEntity executor1 = new UserEntity();
        executor1.setRoles(Set.of(roleUser, roleExecutor));
        executor1.setName("name1");
        executor1.setSurname("surname1");
        executor1.setEmail("email1");

        UserEntity executor2 = new UserEntity();
        executor2.setRoles(Set.of(roleUser, roleExecutor));
        executor2.setName("name2");
        executor2.setSurname("surname2");
        executor2.setEmail("email2");

        UserEntity customer = new UserEntity();
        customer.setRoles(Set.of(roleUser));
        customer.setName("name3");
        customer.setSurname("surname3");
        customer.setEmail("email3");

        userRepository.saveAll(List.of(executor1, executor2, customer));

        ServiceEntity service1 = new ServiceEntity();
        service1.setExecutor(executor1);
        service1.setTitle("title1");

        ServiceEntity service2 = new ServiceEntity();
        service2.setExecutor(executor1);
        service2.setTitle("title2");

        ServiceEntity service3 = new ServiceEntity();
        service3.setExecutor(executor2);
        service3.setTitle("title3");

        serviceRepository.saveAll(List.of(service1, service2, service3));

        OrderEntity order1 = new OrderEntity();
        order1.setStatus(OrderStatus.NEW);
        order1.setExecutor(executor1);
        order1.setService(service1);
        order1.setCustomer(customer);

        OrderEntity order2 = new OrderEntity();
        order2.setStatus(OrderStatus.ACCEPTED);
        order2.setExecutor(executor1);
        order2.setService(service2);
        order2.setCustomer(customer);

        OrderEntity order3 = new OrderEntity();
        order3.setStatus(OrderStatus.COMPLETED);
        order3.setExecutor(executor2);
        order3.setService(service3);
        order3.setCustomer(customer);

        orderRepository.saveAll(List.of(order1, order2, order3));
    }

    @Test
    void shouldFilter_byExecutor() {
        OrderFilter filter = new OrderFilter(1L, null);
        Specification<OrderEntity> spec = OrderSpecification.fromFilter(filter);

        var result = orderRepository.findAll(spec, Pageable.ofSize(20));
        assertThatCollection(result.getContent()).hasSize(2);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void shouldFilter_byStatus() {
        OrderFilter filter = new OrderFilter(null, OrderStatus.NEW);
        Specification<OrderEntity> spec = OrderSpecification.fromFilter(filter);

        var result = orderRepository.findAll(spec, Pageable.ofSize(20));
        assertThatCollection(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void shouldFilter_ByExecutorAndStatus() {
        OrderFilter filter = new OrderFilter(1L, OrderStatus.NEW);
        Specification<OrderEntity> spec = OrderSpecification.fromFilter(filter);

        var result = orderRepository.findAll(spec, Pageable.ofSize(20));
        assertThatCollection(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(result.getContent().getFirst().getExecutor().getId()).isEqualTo(1L);
    }

    @Test
    void shouldNotFilter_whenNull() {
        OrderFilter filter = new OrderFilter(null, null);
        Specification<OrderEntity> spec = OrderSpecification.fromFilter(filter);

        var result = orderRepository.findAll(spec, Pageable.ofSize(20));
        assertThatCollection(result.getContent()).hasSize(3);
    }

    @Test
    void shouldFilter_whenInvalidExecutor() {
        OrderFilter filter = new OrderFilter(-1L, null);
        Specification<OrderEntity> spec = OrderSpecification.fromFilter(filter);

        var result = orderRepository.findAll(spec, Pageable.ofSize(20));
        assertThatCollection(result.getContent()).hasSize(0);
    }
}
