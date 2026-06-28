package org.servicehub.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.servicehub.config.PersistenceTestConfig;
import org.servicehub.dto.filter.UserFilter;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.UserEntity;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.repository.RoleRepository;
import org.servicehub.repository.UserRepository;
import org.servicehub.repository.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@Transactional
public class UserSpecificationAndPaginationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        UserEntity u1 = new UserEntity();
        u1.setName("John");
        u1.setSurname("Smith");
        u1.setEmail("JohnSmith@email.com");
        u1.setPhone("777777777");

        UserEntity u2 = new UserEntity();
        u2.setName("Roman");
        u2.setSurname("Validator");
        u2.setPatronymic("Igorevich");
        u2.setEmail("RomanValidator@email.com");
        u2.setPhone("888888888");

        UserEntity u3 = new UserEntity();
        u3.setName("Valera");
        u3.setSurname("Nekto");
        u3.setPatronymic("Valentynovich");
        u3.setEmail("ValeraNekto@email.com");
        u3.setPhone("999999999");

        RoleEntity roleEntity = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new InvalidRoleException("ex"));
        u3.setRoles(
                Set.of(roleEntity)
        );

        userRepository.saveAll(List.of(u1, u2, u3));
    }

    @Test
    void shouldFilterBySearch() {
        UserFilter filter = new UserFilter("John", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("John");
        assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Smith");
    }

    @Test
    void shouldFilterByPhone() {
        UserFilter filter = new UserFilter(null, "888", null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Roman");
        assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Validator");
    }

    @Test
    void shouldFilterByRoles() {
        UserFilter filter = new UserFilter(null, null, List.of("ADMIN"));
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Valera");
        assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Nekto");
    }

    @Test
    void shouldFilterByPartialSearch() {
        UserFilter filter = new UserFilter("ich", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Roman");
        assertThat(result.getContent().getLast().getName()).isEqualTo("Valera");
    }

    @Test
    void shouldFilterBySearchIgnoringCase() {
        UserFilter filter = new UserFilter("JOHN", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("John");
        assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Smith");
    }

    @Test
    void shouldFilterByPartitioningEmail() {
        UserFilter filter = new UserFilter("@email.com", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void shouldFilterByEmail() {
        UserFilter filter = new UserFilter("JohnSmith@email.com", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("John");
    }

    @Test
    void shouldFilterWhenExtraSpaces() {
        UserFilter filter = new UserFilter("   John     ", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("John");
    }

    @Test
    void shouldFilterWhenNonExists() {
        UserFilter filter = new UserFilter("Nonexisted", null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    void shouldFilterWhenAllNull() {
        UserFilter filter = new UserFilter(null, null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void shouldRightSort() {
        UserFilter filter = new UserFilter(null, null, null);
        Specification<UserEntity> spec = UserSpecification.fromFilter(filter);
        Page<UserEntity> result = userRepository.findAll(spec, PageRequest.of(0, 10)
                .withSort(Sort.by(Sort.Direction.DESC,"phone")));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Valera");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Roman");
        assertThat(result.getContent().getLast().getName()).isEqualTo("John");
    }
}
