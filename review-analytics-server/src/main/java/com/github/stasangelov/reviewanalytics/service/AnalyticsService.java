package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.dto.KpiDto;
import com.github.stasangelov.reviewanalytics.dto.TopProductDto;
import com.github.stasangelov.reviewanalytics.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    @PersistenceContext
    private final EntityManager entityManager;

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

        return dashboard;
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

        jpqlBuilder.append(" GROUP BY r.product.id, r.product.name HAVING COUNT(r) >= 5 ORDER BY avg_rating ").append(direction);

        TypedQuery<TopProductDto> query = entityManager.createQuery(jpqlBuilder.toString(), TopProductDto.class);
        parameters.forEach(query::setParameter);

        return query.setMaxResults(limit).getResultList();
    }
}
