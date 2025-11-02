package com.github.stasangelov.reviewanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс и точка входа для серверного Spring Boot приложения.
 */
@SpringBootApplication
public class ReviewAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewAnalyticsApplication.class, args);
    }
}
