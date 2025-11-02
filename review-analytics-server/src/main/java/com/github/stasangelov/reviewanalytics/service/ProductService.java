package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.dictionary.ProductDto;
import com.github.stasangelov.reviewanalytics.entity.Product;
import com.github.stasangelov.reviewanalytics.repository.CategoryRepository;
import com.github.stasangelov.reviewanalytics.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сущностями "Товар" ({@link Product}).
 * Предоставляет методы для получения списка товаров и преобразования их в DTO.
 * Все операции по умолчанию выполняются в транзакции только для чтения.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> getAll() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        return dto;
    }

}
