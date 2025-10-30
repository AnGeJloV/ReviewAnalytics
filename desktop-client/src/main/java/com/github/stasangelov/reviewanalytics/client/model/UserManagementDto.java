package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserManagementDto {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Set<String> roles;

    public String getRole() {
        if (roles == null || roles.isEmpty()) {
            return "N/A";
        }
        return roles.iterator().next();
    }
}
