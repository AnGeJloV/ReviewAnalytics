package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.dto.KpiDto;
import com.github.stasangelov.reviewanalytics.dto.TopProductDto;
import com.github.stasangelov.reviewanalytics.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    @PersistenceContext
    private final EntityManager entityManager;

    public DashboardDto getDashboardData() {
        DashboardDto dashboard = new DashboardDto();

        dashboard.setKpis(calculateKpis());
        dashboard.setTopRatedProducts(findTopRatedProducts(5, "DESC")); // по убыванию (лучшие)
        dashboard.setWorstRatedProducts(findTopRatedProducts(5, "ASC")); // по возрастанию (худшие)

        return dashboard;
    }

    /**
     * Рассчитывает ключевые показатели эффективности (KPI).
     */
    private KpiDto calculateKpis() {
        // JPQL-запрос для подсчета количества и среднего рейтинга всех АКТИВНЫХ отзывов
        String jpql = "SELECT COUNT(r), AVG(r.integralRating) FROM Review r WHERE r.status = :status";

        Object[] result = entityManager.createQuery(jpql, Object[].class)
                .setParameter("status", Review.ReviewStatus.ACTIVE)
                .getSingleResult();

        KpiDto kpi = new KpiDto();
        kpi.setTotalReviews((long) (result[0] != null ? result[0] : 0L));
        kpi.setAverageIntegralRating((double) (result[1] != null ? result[1] : 0.0));

        return kpi;
    }

    /**
     * Находит топ-N лучших или худших товаров.
     * @param limit Количество товаров для выборки.
     * @param direction "DESC" для лучших, "ASC" для худших.
     */
    private List<TopProductDto> findTopRatedProducts(int limit, String direction) {
        // Сложный JPQL-запрос:
        // 1. Группируем отзывы по товару (r.product).
        // 2. Считаем средний рейтинг (AVG) и количество отзывов (COUNT) для каждой группы.
        // 3. Фильтруем, оставляя только те товары, у которых отзывов > 5 (требование).
        // 4. Сортируем по среднему рейтингу в указанном направлении.
        String jpql = "SELECT NEW com.github.stasangelov.reviewanalytics.dto.TopProductDto(" +
                "   r.product.id, " +
                "   r.product.name, " +
                "   AVG(r.integralRating) as avg_rating" +
                ") " +
                "FROM Review r " +
                "WHERE r.status = :status " +
                "GROUP BY r.product.id, r.product.name " +
                "HAVING COUNT(r) >= 5 " +
                "ORDER BY avg_rating " + direction;

        return entityManager.createQuery(jpql, TopProductDto.class)
                .setParameter("status", Review.ReviewStatus.ACTIVE)
                .setMaxResults(limit) // Ограничиваем количество результатов (Топ-5)
                .getResultList();
    }
}
