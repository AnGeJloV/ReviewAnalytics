package com.github.stasangelov.reviewanalytics.client.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDto {
    private Long id;
    private String productName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreated;

    private String status;
    private Double integralRating;

    private Long productId;
    private Map<Long, Integer> ratings;
    private List<ReviewRatingDto> reviewRatings;
}