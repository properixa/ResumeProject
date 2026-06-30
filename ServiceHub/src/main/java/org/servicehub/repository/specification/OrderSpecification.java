package org.servicehub.repository.specification;

import org.servicehub.dto.filter.OrderFilter;
import org.servicehub.entity.OrderEntity;
import org.servicehub.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
    public static Specification<OrderEntity> filterStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> filterByExecutor(Long id) {
        return (root, query, cb) -> id == null ? null
                : cb.equal(root.get("executor").get("id"), id);
    }

    public static Specification<OrderEntity> fromFilter(OrderFilter filter) {
        return Specification.allOf(
                filterStatus(filter.status()),
                filterByExecutor(filter.executorId())
        );

    }
}
