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
 * REST-контроллер для получения критериев (Criterion).
 */

@RestController
@RequestMapping("/api/criteria")
@RequiredArgsConstructor
public class CriterionController {
    private final CriterionService criterionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<CriterionDto>> getAllCriteria() {
        return ResponseEntity.ok(criterionService.getAll());
    }

}
