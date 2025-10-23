package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.CriterionDto;
import com.github.stasangelov.reviewanalytics.service.CriterionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления критериями (Criterion).
 * Доступ к этому контроллеру разрешен только пользователям с ролью 'ADMIN'.
 */

@RestController
@RequestMapping("/api/criteria")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class CriterionController {
    private final CriterionService criterionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<CriterionDto>> getAllCriteria() {
        return ResponseEntity.ok(criterionService.getAll());
    }

    @PostMapping
    public ResponseEntity<CriterionDto> createCriterion(@Valid @RequestBody CriterionDto criterionDto) {
        return new ResponseEntity<>(criterionService.create(criterionDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CriterionDto> updateCriterion(@PathVariable Long id, @Valid @RequestBody CriterionDto criterionDto) {
        return ResponseEntity.ok(criterionService.update(id, criterionDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCriterion(@PathVariable Long id) {
        criterionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
