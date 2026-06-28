package org.servicehub.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.servicehub.dto.filter.UserFilter;
import org.servicehub.entity.RoleEntity;
import org.servicehub.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    // find by name, surname, patronymic, email
    public static Specification<UserEntity> search(String search) {
        return (root, query, cb) -> {
            if (search == null) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String token : search.split("\\s+")) {
                String pattern = "%" + token + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern.toLowerCase()),
                                cb.like(cb.lower(root.get("surname")), pattern.toLowerCase()),
                                cb.like(cb.lower(root.get("patronymic")), pattern.toLowerCase()),
                                cb.like(root.get("email"), pattern)
                        )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserEntity> phoneContains(String phone) {
        return (root, query, cb) -> phone == null ? null : cb.like(
                root.get("phone"), "%" + phone + "%"
        );
    }

    public static Specification<UserEntity> hasRoles(List<String> roles) {
        return (root, query, cb) -> {
            if (roles == null) {
                return null;
            }

            Join<UserEntity, RoleEntity> roleJoin = root.join("roles");

            return roleJoin.get("name").in(roles);
        };
    }

    public static Specification<UserEntity> fromFilter(UserFilter filter) {
        return Specification.allOf(
                search(filter.search()),
                phoneContains(filter.phone()),
                hasRoles(filter.roles())
        );
    }
}
