package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDto {
    private KpiDto kpis;
    private List<TopProductDto> topRatedProducts;
    private List<TopProductDto> worstRatedProducts;
    // TODO: Добавить поля для других графиков (сравнение категорий, динамика)
}
