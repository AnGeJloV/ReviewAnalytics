package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link Role}.
 */

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
}
