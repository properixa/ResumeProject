package org.servicehub.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    NEW,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static Optional<OrderStatus> fromString(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equals(value))
                .findFirst();
    }
}
