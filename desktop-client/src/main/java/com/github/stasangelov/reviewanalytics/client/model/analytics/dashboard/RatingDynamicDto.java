package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO для одной точки на графике динамики рейтинга.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingDynamicDto {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;
    private Double averageRating;
}
