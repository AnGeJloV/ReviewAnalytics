package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link Category}.
 */

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
}
