package com.github.stasangelov.reviewanalytics.client.model;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class ReviewDto {
    private Long id;
    private String productName;
    private LocalDate dateCreated;
    private String status;
    private Long productId;
    private Map<Long, Integer> ratings;
}