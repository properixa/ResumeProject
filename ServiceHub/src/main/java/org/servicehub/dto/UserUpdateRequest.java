package org.servicehub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.servicehub.validation.groups.First;
import org.servicehub.validation.groups.Second;

import java.util.Set;

public record UserUpdateRequest(
        @NotBlank(groups = First.class, message = "name should exist")
        @Pattern(groups = Second.class, regexp = "^[a-zA-Zа-яА-ЯёЁ'-]{2,} [a-zA-Zа-яА-ЯёЁ'-]{2,}( [a-zA-Zа-яА-ЯёЁ'-]{2,})?$",
                message = "must contains name, surname in right format")
        String fullName,
        @NotBlank(groups = First.class, message = "email should exist")
        @Email(groups = Second.class, message = "email should be in right format")
        String email,
        @NotBlank(groups = First.class, message = "phone should exist")
        @Pattern(groups = Second.class, regexp = "^\\d{7,15}$", message = "must contains phone number")
        String phone,
        Set<String> roles) {
}
