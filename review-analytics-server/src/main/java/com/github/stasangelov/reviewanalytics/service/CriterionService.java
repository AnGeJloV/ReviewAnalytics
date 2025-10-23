package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.CriterionDto;
import com.github.stasangelov.reviewanalytics.entity.Criterion;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
import com.github.stasangelov.reviewanalytics.repository.CriterionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сущностями Criterion (критерии оценки).
 * Предоставляет полный набор CRUD-операций и логику преобразования между Entity и DTO.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CriterionService {

    private final CriterionRepository criterionRepository;

    public List<CriterionDto> getAll() {
        return criterionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CriterionDto toDto(Criterion criterion) {
        CriterionDto dto = new CriterionDto();
        dto.setId(criterion.getId());
        dto.setName(criterion.getName());
        dto.setWeight(criterion.getWeight());
        return dto;
    }
}
