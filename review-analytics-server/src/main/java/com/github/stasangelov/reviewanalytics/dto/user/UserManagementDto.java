package com.github.stasangelov.reviewanalytics.dto.user;

import com.github.stasangelov.reviewanalytics.entity.Role;
import lombok.Data;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO для отображения информации о пользователе на странице управления.
 */
@Data
public class UserManagementDto {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Set<String> roles;

    public UserManagementDto(Long id, String name, String email, boolean active, Set<Role> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.roles = roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }
}
