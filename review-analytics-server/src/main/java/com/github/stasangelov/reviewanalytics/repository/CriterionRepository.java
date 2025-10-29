package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Criterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для доступа к данным сущности {@link Criterion}.
 */

@Repository
public interface CriterionRepository extends JpaRepository<Criterion,Long> {
    /**
     * Находит все критерии, связанные с указанной категорией.
     * Spring Data JPA автоматически построит сложный запрос с JOIN'ом по имени метода.
     */
    List<Criterion> findByCategories_Id(Long categoryId);
}
