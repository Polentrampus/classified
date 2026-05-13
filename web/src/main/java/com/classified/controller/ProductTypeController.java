package com.classified.controller;

import com.classified.dto.adType.ProductTypeResponse;
import com.classified.service.ProductTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productTypes")
@RequiredArgsConstructor
@Tag(name = "Product Types", description = "Управление типами продуктов (ADMIN)")
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    @Operation(summary = "Создать тип продукта")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductTypeResponse> create(@RequestParam String name) {
        ProductTypeResponse response = productTypeService.create(name);
        return ResponseEntity.created(URI.create("/api/product-types/" + response.getId())).body(response);
    }

    @Operation(summary = "Обновить тип продукта")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductTypeResponse> update(@PathVariable Long id, @RequestParam String name) {
        return ResponseEntity.ok(productTypeService.update(id, name));
    }

    @Operation(summary = "Удалить тип продукта")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить тип продукта по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ProductTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productTypeService.getById(id));
    }

    @Operation(summary = "Получить все типы продуктов")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<ProductTypeResponse>> getAll() {
        return ResponseEntity.ok(productTypeService.getAll());
    }
}