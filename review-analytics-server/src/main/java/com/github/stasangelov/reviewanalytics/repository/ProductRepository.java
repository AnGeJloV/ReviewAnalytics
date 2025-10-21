package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link Product}.
 * Обеспечивает выполнение CRUD-операций для товаров.
 */

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
}
