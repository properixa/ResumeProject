package org.servicehub.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.servicehub.validation.groups.FirstGroup;
import org.servicehub.validation.groups.SecondGroup;

import java.util.Set;

public record UserUpdateRequest(
        @NotBlank(groups = FirstGroup.class, message = "name should exist")
        @Pattern(groups = SecondGroup.class, regexp = "^[a-zA-Zа-яА-ЯёЁ'-]{2,} [a-zA-Zа-яА-ЯёЁ'-]{2,}( [a-zA-Zа-яА-ЯёЁ'-]{2,})?$",
                message = "must contains name, surname in right format")
        String fullName,
        String password,
        @NotBlank(groups = FirstGroup.class, message = "email should exist")
        @Email(groups = SecondGroup.class, message = "email should be in right format")
        String email,
        @NotBlank(groups = FirstGroup.class, message = "phone should exist")
        @Pattern(groups = SecondGroup.class, regexp = "^\\d{7,15}$", message = "must contains phone number")
        String phone,
        Set<String> roles) {
}
