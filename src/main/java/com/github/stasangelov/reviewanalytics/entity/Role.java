package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность, представляющая роль пользователя в системе.
 * Определяет уровень доступа и права пользователя.
 */

@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleName name;

    public enum RoleName {
        ANALYST,
        ADMIN
    }
}