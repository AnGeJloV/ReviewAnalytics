package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Criterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link Criterion}.
 */

@Repository
public interface CriterionRepository extends JpaRepository<Criterion,Long> {
}
