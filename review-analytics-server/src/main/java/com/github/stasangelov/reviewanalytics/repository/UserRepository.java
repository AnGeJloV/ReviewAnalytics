package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для доступа к данным сущности {@link User}.
 * Предоставляет стандартные CRUD-операции и кастомные методы для поиска пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
