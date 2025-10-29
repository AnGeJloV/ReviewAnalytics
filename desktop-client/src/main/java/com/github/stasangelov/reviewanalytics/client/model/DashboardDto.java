package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardDto {
    private KpiDto kpis;
    private List<TopProductDto> topRatedProducts;
    private List<TopProductDto> worstRatedProducts;
}
