package com.classified.controller;

import com.classified.dto.adType.AdTypeCreateRequest;
import com.classified.dto.adType.AdTypeResponse;
import com.classified.service.AdTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/adTypes")
@RequiredArgsConstructor
@Tag(name = "Ad Types", description = "Управление типами объявлений (связи категорий и типов продуктов) (ADMIN)")
public class AdTypeController {

    private final AdTypeService adTypeService;

    @Operation(summary = "Создать связь типа объявления",
            description = """
                    Создаёт связь между категорией и типом продукта.
                    Пример:
                    {
                      "productTypeId": 1,
                      "categoryId": 1
                    }
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Связь создана"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "409", description = "Связь уже существует")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdTypeResponse> create(@RequestBody @Valid AdTypeCreateRequest request) {
        AdTypeResponse response = adTypeService.create(request);
        return ResponseEntity.created(URI.create("/api/ad-types/" + response.getId())).body(response);
    }

    @Operation(summary = "Удалить связь типа объявления")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить связь по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adTypeService.getById(id));
    }

    @Operation(summary = "Получить все связи")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdTypeResponse>> getAll() {
        return ResponseEntity.ok(adTypeService.getAll());
    }

    @Operation(summary = "Получить связи по категории")
    @GetMapping("/byCategory/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdTypeResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(adTypeService.getByCategoryId(categoryId));
    }

    @Operation(summary = "Получить связи по типу продукта")
    @GetMapping("/byProductType/{productTypeId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdTypeResponse>> getByProductTypeId(@PathVariable Long productTypeId) {
        return ResponseEntity.ok(adTypeService.getByProductTypeId(productTypeId));
    }
}