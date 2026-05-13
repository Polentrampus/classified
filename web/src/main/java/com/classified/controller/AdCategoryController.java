package com.classified.controller;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.service.AdCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Управление категориями объявлений (ADMIN)")
public class AdCategoryController {

    private final AdCategoryService adCategoryService;

    @Operation(summary = "Создать категорию")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdCategoryResponse> create(@RequestParam String name) {
        AdCategoryResponse response = adCategoryService.create(name);
        return ResponseEntity.created(URI.create("/api/categories/" + response.getId())).body(response);
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdCategoryResponse> update(@PathVariable Long id, @RequestParam String name) {
        return ResponseEntity.ok(adCategoryService.update(id, name));
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить категорию по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adCategoryService.getById(id));
    }

    @Operation(summary = "Получить все категории")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdCategoryResponse>> getAll() {
        return ResponseEntity.ok(adCategoryService.getAll());
    }
}