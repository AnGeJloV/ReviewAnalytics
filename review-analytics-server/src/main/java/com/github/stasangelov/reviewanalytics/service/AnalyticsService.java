package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.*;
import com.github.stasangelov.reviewanalytics.entity.Product;
import com.github.stasangelov.reviewanalytics.entity.Review;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.stasangelov.reviewanalytics.service.mapper.ReviewMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.stasangelov.reviewanalytics.dto.ComparisonDataDto;
import com.github.stasangelov.reviewanalytics.entity.Product;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final ReviewMapper reviewMapper;

    /**
     * Главный метод, который собирает все данные для дашборда с учетом фильтров.
     */
    public DashboardDto getDashboardData(LocalDate startDate, LocalDate endDate, Long categoryId) {
        DashboardDto dashboard = new DashboardDto();

        // Преобразуем LocalDate в LocalDateTime для запросов к БД
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        dashboard.setKpis(calculateKpis(startDateTime, endDateTime, categoryId));
        dashboard.setTopRatedProducts(findTopRatedProducts(5, "DESC", startDateTime, endDateTime, categoryId));
        dashboard.setWorstRatedProducts(findTopRatedProducts(5, "ASC", startDateTime, endDateTime, categoryId));

        if (categoryId != null) {
            // Если категория выбрана, считаем рейтинг по брендам ВНУТРИ нее
            dashboard.setBrandRatings(calculateBrandRatings(startDateTime, endDateTime, categoryId));
            dashboard.setCategoryRatings(null); // Обнуляем данные для другого графика
        } else {
            // Если категория не выбрана, считаем рейтинг по категориям
            dashboard.setCategoryRatings(calculateCategoryRatings(startDateTime, endDateTime, null));
            dashboard.setBrandRatings(null);
        }
        dashboard.setRatingDynamics(calculateRatingDynamics(startDateTime, endDateTime, categoryId));
        dashboard.setRatingDistribution(calculateRatingDistribution(startDateTime, endDateTime, categoryId));

        return dashboard;
    }

    /**
     * НОВЫЙ МЕТОД: Возвращает сводную аналитику по всем товарам для таблицы.
     */
    public List<ProductSummaryDto> getProductsSummary(LocalDate startDate, LocalDate endDate, Long categoryId) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        StringBuilder jpqlBuilder = new StringBuilder(
                "SELECT NEW com.github.stasangelov.reviewanalytics.dto.ProductSummaryDto(" +
                        "   p.id, p.name, p.category.name, p.brand, COUNT(r), AVG(r.integralRating)" +
                        ") " +
                        "FROM Product p LEFT JOIN Review r ON r.product = p AND r.status = 'ACTIVE' "
        );

        // Динамически добавляем WHERE условия
        Map<String, Object> parameters = new HashMap<>();
        boolean hasWhere = false;

        if (startDateTime != null) {
            jpqlBuilder.append(" WHERE r.dateCreated >= :startDate");
            parameters.put("startDate", startDateTime);
            hasWhere = true;
        }
        if (endDateTime != null) {
            jpqlBuilder.append(hasWhere ? " AND" : " WHERE").append(" r.dateCreated < :endDate");
            parameters.put("endDate", endDateTime);
            hasWhere = true;
        }
        if (categoryId != null) {
            jpqlBuilder.append(hasWhere ? " AND" : " WHERE").append(" p.category.id = :categoryId");
            parameters.put("categoryId", categoryId);
        }

        jpqlBuilder.append(" GROUP BY p.id, p.name, p.category.name, p.brand ORDER BY p.name ASC");

        TypedQuery<ProductSummaryDto> query = entityManager.createQuery(jpqlBuilder.toString(), ProductSummaryDto.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * НОВЫЙ МЕТОД: Возвращает полную детализацию по одному товару.
     */
    public ProductDetailsDto getProductDetails(Long productId) {
        // 1. Находим профиль по критериям (средние оценки)
        String profileJpql = "SELECT NEW com.github.stasangelov.reviewanalytics.dto.CriteriaProfileDto(" +
                "   rr.criterion.name, AVG(rr.rating)" +
                ") " +
                "FROM ReviewRating rr JOIN rr.review r " +
                "WHERE r.product.id = :productId AND r.status = 'ACTIVE' " +
                "GROUP BY rr.criterion.name";
        List<CriteriaProfileDto> criteriaProfile = entityManager.createQuery(profileJpql, CriteriaProfileDto.class)
                .setParameter("productId", productId)
                .getResultList();

        // 2. Находим все отзывы на этот товар
        String reviewsJpql = "SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.dateCreated DESC";
        List<Review> reviews = entityManager.createQuery(reviewsJpql, Review.class)
                .setParameter("productId", productId)
                .getResultList();

        // 3. Собираем все в один DTO
        Product product = reviews.isEmpty() ?
                entityManager.find(Product.class, productId) :
                reviews.get(0).getProduct();

        if (product == null) {
            throw new ResourceNotFoundException("Товар с id " + productId + " не найден");
        }

        ProductDetailsDto details = new ProductDetailsDto();
        details.setProductId(product.getId());
        details.setProductName(product.getName());
        details.setCategoryName(product.getCategory().getName());
        details.setBrand(product.getBrand());
        details.setReviewCount((long) reviews.size());

        // Считаем общий средний рейтинг
        double avgRating = reviews.stream()
                .filter(r -> r.getStatus() == Review.ReviewStatus.ACTIVE && r.getIntegralRating() != null)
                .mapToDouble(Review::getIntegralRating)
                .average()
                .orElse(0.0);
        details.setAverageRating(avgRating);

        details.setCriteriaProfile(criteriaProfile);
        details.setReviews(reviews.stream().map(reviewMapper::toDto).collect(Collectors.toList()));

        return details;
    }


    /**
     * НОВЫЙ МЕТОД: Рассчитывает распределение оценок (1-5) по каждому критерию.
     */
    private List<RatingDistributionDto> calculateRatingDistribution(LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        // Этот запрос проще сделать нативным SQL с использованием CASE
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT c.name as criterion_name, " +
                        "   COUNT(CASE WHEN rr.rating = 1 THEN 1 END) as count1, " +
                        "   COUNT(CASE WHEN rr.rating = 2 THEN 1 END) as count2, " +
                        "   COUNT(CASE WHEN rr.rating = 3 THEN 1 END) as count3, " +
                        "   COUNT(CASE WHEN rr.rating = 4 THEN 1 END) as count4, " +
                        "   COUNT(CASE WHEN rr.rating = 5 THEN 1 END) as count5 " +
                        "FROM review_ratings rr " +
                        "JOIN criteria c ON rr.criterion_id = c.id " +
                        "JOIN reviews r ON rr.review_id = r.id " +
                        "JOIN products p ON r.product_id = p.id " +
                        "WHERE r.status = 'ACTIVE'"
        );
        Map<String, Object> parameters = new HashMap<>();

        if (startDateTime != null) {
            sqlBuilder.append(" AND r.date_created >= :startDate");
            parameters.put("startDate", startDateTime);
        }
        if (endDateTime != null) {
            sqlBuilder.append(" AND r.date_created < :endDate");
            parameters.put("endDate", endDateTime);
        }
        if (categoryId != null) {
            sqlBuilder.append(" AND p.category_id = :categoryId");
            parameters.put("categoryId", categoryId);
        }

        sqlBuilder.append(" GROUP BY criterion_name ORDER BY criterion_name");

        List<Object[]> results = entityManager.createNativeQuery(sqlBuilder.toString())
                .unwrap(org.hibernate.query.Query.class)
                .setProperties(parameters)
                .getResultList();

        // Вручную преобразуем результат в DTO
        List<RatingDistributionDto> distributionList = new ArrayList<>();
        for (Object[] row : results) {
            RatingDistributionDto dto = new RatingDistributionDto((String) row[0]);
            dto.setRating1Count(((Number) row[1]).longValue());
            dto.setRating2Count(((Number) row[2]).longValue());
            dto.setRating3Count(((Number) row[3]).longValue());
            dto.setRating4Count(((Number) row[4]).longValue());
            dto.setRating5Count(((Number) row[5]).longValue());
            distributionList.add(dto);
        }
        return distributionList;
    }


    /**
     * НОВЫЙ МЕТОД: Рассчитывает средний рейтинг для каждого бренда в рамках одной категории.
     */
    private List<BrandRatingDto> calculateBrandRatings(LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        StringBuilder jpqlBuilder = new StringBuilder(
                "SELECT NEW com.github.stasangelov.reviewanalytics.dto.BrandRatingDto(" +
                        "   r.product.brand, " +
                        "   AVG(r.integralRating)" +
                        ") " +
                        "FROM Review r WHERE r.status = :status"
        );
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Review.ReviewStatus.ACTIVE);

        // Применяем все фильтры, включая обязательный categoryId
        addQueryFilters(jpqlBuilder, parameters, startDateTime, endDateTime, categoryId);

        jpqlBuilder.append(" GROUP BY r.product.brand ORDER BY AVG(r.integralRating) DESC");

        TypedQuery<BrandRatingDto> query = entityManager.createQuery(jpqlBuilder.toString(), BrandRatingDto.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * Рассчитывает средний рейтинг для каждой категории.
     */
    private List<CategoryRatingDto> calculateCategoryRatings(LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        StringBuilder jpqlBuilder = new StringBuilder(
                "SELECT NEW com.github.stasangelov.reviewanalytics.dto.CategoryRatingDto(" +
                        "   r.product.category.name, " +
                        "   AVG(r.integralRating)" +
                        ") " +
                        "FROM Review r WHERE r.status = :status"
        );
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Review.ReviewStatus.ACTIVE);

        addQueryFilters(jpqlBuilder, parameters, startDateTime, endDateTime, categoryId);

        jpqlBuilder.append(" GROUP BY r.product.category.name ORDER BY AVG(r.integralRating) DESC");

        TypedQuery<CategoryRatingDto> query = entityManager.createQuery(jpqlBuilder.toString(), CategoryRatingDto.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * Рассчитывает динамику среднего рейтинга с АДАПТИВНОЙ гранулярностью (день/неделя/месяц)
     * в зависимости от выбранного периода.
     */
    private List<RatingDynamicDto> calculateRatingDynamics(LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        // 1. Определяем длительность периода
        long daysBetween = 90; // Значение по умолчанию, если даты не заданы
        if (startDateTime != null && endDateTime != null) {
            daysBetween = ChronoUnit.DAYS.between(startDateTime, endDateTime);
        }

        // 2. Выбираем, как будем группировать дату (гранулярность)
        String dateGroupingSql;
        if (daysBetween <= 15) {
            // Группировка по ДНЯМ
            dateGroupingSql = "CAST(r.date_created AS DATE)";
        } else if (daysBetween <= 60) {
            // Группировка по НЕДЕЛЯМ (ISO week format)
            dateGroupingSql = "STR_TO_DATE(CONCAT(YEAR(r.date_created), '-', WEEK(r.date_created, 1), '-1'), '%Y-%U-%w')";
        } else {
            // Группировка по МЕСЯЦАМ
            dateGroupingSql = "STR_TO_DATE(CONCAT(YEAR(r.date_created), '-', MONTH(r.date_created), '-01'), '%Y-%m-%d')";
        }

        // 3. Строим SQL-запрос с выбранной группировкой
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT " + dateGroupingSql + " as group_date, AVG(r.integral_rating) as avg_rating " +
                        "FROM reviews r JOIN products p ON r.product_id = p.id " +
                        "WHERE r.status = 'ACTIVE'"
        );
        Map<String, Object> parameters = new HashMap<>();

        // Добавляем фильтры
        if (startDateTime != null) {
            sqlBuilder.append(" AND r.date_created >= :startDate");
            parameters.put("startDate", startDateTime);
        }
        if (endDateTime != null) {
            sqlBuilder.append(" AND r.date_created < :endDate");
            parameters.put("endDate", endDateTime);
        }
        if (categoryId != null) {
            sqlBuilder.append(" AND p.category_id = :categoryId");
            parameters.put("categoryId", categoryId);
        }

        // Группируем и сортируем по вычисленной дате
        sqlBuilder.append(" GROUP BY group_date ORDER BY group_date ASC");

        // 4. Выполняем запрос и преобразуем результат
        List<Object[]> results = entityManager.createNativeQuery(sqlBuilder.toString())
                .unwrap(org.hibernate.query.Query.class)
                .setProperties(parameters)
                .getResultList();

        return results.stream()
                .map(row -> new RatingDynamicDto(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).doubleValue()
                ))
                .collect(Collectors.toList());

    }

    /**
     * Рассчитывает ключевые показатели эффективности (KPI).
     */
    private KpiDto calculateKpis(LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        // Используем StringBuilder для динамического построения запроса
        StringBuilder jpqlBuilder = new StringBuilder("SELECT COUNT(r), AVG(r.integralRating) FROM Review r WHERE r.status = :status");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Review.ReviewStatus.ACTIVE);

        // Добавляем условия в запрос, если фильтры заданы
        addQueryFilters(jpqlBuilder, parameters, startDateTime, endDateTime, categoryId);

        TypedQuery<Object[]> query = entityManager.createQuery(jpqlBuilder.toString(), Object[].class);
        parameters.forEach(query::setParameter);

        Object[] result = query.getSingleResult();

        KpiDto kpi = new KpiDto();
        kpi.setTotalReviews((long) (result[0] != null ? result[0] : 0L));
        kpi.setAverageIntegralRating((double) (result[1] != null ? result[1] : 0.0));
        return kpi;
    }

    /**
     * Вспомогательный метод, который добавляет WHERE условия к JPQL запросу.
     */
    private void addQueryFilters(StringBuilder jpqlBuilder, Map<String, Object> parameters, LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        if (startDateTime != null) {
            jpqlBuilder.append(" AND r.dateCreated >= :startDate");
            parameters.put("startDate", startDateTime);
        }
        if (endDateTime != null) {
            jpqlBuilder.append(" AND r.dateCreated < :endDate");
            parameters.put("endDate", endDateTime);
        }
        if (categoryId != null) {
            // Для фильтрации по категории нам нужно сделать JOIN
            jpqlBuilder.append(" AND r.product.category.id = :categoryId");
            parameters.put("categoryId", categoryId);
        }
    }

    /**
     * Находит топ-N лучших или худших товаров.
     * @param limit Количество товаров для выборки.
     * @param direction "DESC" для лучших, "ASC" для худших.
     */
    private List<TopProductDto> findTopRatedProducts(int limit, String direction, LocalDateTime startDateTime, LocalDateTime endDateTime, Long categoryId) {
        StringBuilder jpqlBuilder = new StringBuilder(
                "SELECT NEW com.github.stasangelov.reviewanalytics.dto.TopProductDto(" +
                        "r.product.id, r.product.name, AVG(r.integralRating) as avg_rating) " +
                        "FROM Review r WHERE r.status = :status"
        );
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Review.ReviewStatus.ACTIVE);

        // Добавляем те же самые фильтры
        addQueryFilters(jpqlBuilder, parameters, startDateTime, endDateTime, categoryId);

        jpqlBuilder.append(" GROUP BY r.product.id, r.product.name ORDER BY avg_rating ").append(direction);

        TypedQuery<TopProductDto> query = entityManager.createQuery(jpqlBuilder.toString(), TopProductDto.class);
        parameters.forEach(query::setParameter);

        return query.setMaxResults(limit).getResultList();
    }
    /**
     * НОВЫЙ МЕТОД: Собирает данные для сравнения нескольких товаров.
     * @param productIds Список ID товаров для сравнения.
     * @return Список DTO, где каждый элемент содержит профиль одного товара.
     */
    public List<ComparisonDataDto> getComparisonData(List<Long> productIds) {
        return productIds.stream()
                .map(this::getProductProfileForComparison)
                .collect(Collectors.toList());
    }

    /**
     * Вспомогательный метод для получения профиля одного товара.
     */
    private ComparisonDataDto getProductProfileForComparison(Long productId) {
        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            throw new ResourceNotFoundException("Товар с id " + productId + " не найден");
        }

        String jpql = "SELECT NEW com.github.stasangelov.reviewanalytics.dto.CriteriaProfileDto(" +
                "   rr.criterion.name, AVG(rr.rating)" +
                ") " +
                "FROM ReviewRating rr JOIN rr.review r " +
                "WHERE r.product.id = :productId AND r.status = 'ACTIVE' " +
                "GROUP BY rr.criterion.name";

        List<CriteriaProfileDto> profile = entityManager.createQuery(jpql, CriteriaProfileDto.class)
                .setParameter("productId", productId)
                .getResultList();

        ComparisonDataDto dto = new ComparisonDataDto();
        dto.setProductId(productId);
        dto.setProductName(product.getName());
        dto.setCriteriaProfile(profile);
        return dto;
    }
}
