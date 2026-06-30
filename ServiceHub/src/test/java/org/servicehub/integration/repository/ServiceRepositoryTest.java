package org.servicehub.integration.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.servicehub.config.PersistenceTestConfig;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.ServiceEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.repository.RoleRepository;
import org.servicehub.repository.ServiceRepository;
import org.servicehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.CollectionAssert.assertThatCollection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@Transactional
public class ServiceRepositoryTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE users, services RESTART IDENTITY CASCADE")
                .executeUpdate();
        RoleEntity roleExecutor = roleRepository.findByName("EXECUTOR")
                .orElseThrow(() -> new InvalidRoleException("role not found"));

        UserEntity executor1 = new UserEntity();
        executor1.setName("name1");
        executor1.setSurname("surname1");
        executor1.setEmail("email1");
        executor1.setPhone("79999999");
        executor1.setRoles(Set.of(roleExecutor));

        UserEntity executor2 = new UserEntity();
        executor2.setName("name2");
        executor2.setSurname("surname2");
        executor2.setEmail("email2");
        executor2.setPhone("899999999");
        executor2.setRoles(Set.of(roleExecutor));

        userRepository.saveAll(List.of(executor1, executor2));

        ServiceEntity service1 = new ServiceEntity();
        service1.setTitle("title1");
        service1.setExecutor(executor1);

        ServiceEntity service2 = new ServiceEntity();
        service2.setTitle("title2");
        service2.setExecutor(executor1);

        ServiceEntity service3 = new ServiceEntity();
        service3.setTitle("title3");
        service3.setExecutor(executor2);

        serviceRepository.saveAll(List.of(service1, service2, service3));
    }

    @Test
    void shouldReturnRightServicesWithExecutorId() {
        var result = serviceRepository.findAllByExecutorId(1L, Pageable.unpaged());

        assertThatCollection(result.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenExecutorIdInvalid() {
        var result = serviceRepository.findAllByExecutorId(-5L, Pageable.unpaged());

        assertThatCollection(result.getContent()).isEmpty();
    }
}
