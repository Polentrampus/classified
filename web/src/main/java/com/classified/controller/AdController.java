package com.classified.controller;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.dto.AdStatus;
import com.classified.service.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.classified.security.UserDetailsImpl;
import com.classified.pagination.Direction;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;
import com.classified.pagination.Sort;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Ad", description = "Создание, поиск и управление объявлениями")
public class AdController {

    private final AdService adService;

    @Operation(
            summary = "Создать объявление",
            description = "Продавцом автоматически становится текущий пользователь" +
                    """
                            {
                              "title": "iPhone 15 Pro",
                              "description": "Новый iPhone, полный комплект",
                              "adTypeId": 1
                              "price": 89990.00,
                              "quantity": 1,
                              "adStatus": "ACTIVE",
                              "sellerId": 1,
                              "addressId": 1
                            }
                            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Объявление создано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> createAd(
            @RequestBody @Valid AdCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AdResponse response = adService.createAd(request, userDetails);
        return ResponseEntity.created(URI.create("/api/ads/" + response.getId())).body(response);
    }

    @Operation(
            summary = "Обновить объявление",
            description = "Только владелец или администратор может обновить объявление"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Объявление обновлено"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> updateAd(
            @PathVariable @Parameter(description = "ID объявления") Long id,
            @RequestBody @Valid AdUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(adService.updateAd(id, request, userDetails));
    }

    @Operation(summary = "Получить объявление по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Объявление найдено"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> getAd(
            @PathVariable @Parameter(description = "ID объявления") Long id) {
        return ResponseEntity.ok(adService.getAd(id));
    }

    @Operation(summary = "Удалить объявление")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Объявление удалено"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> deleteAd(
            @PathVariable @Parameter(description = "ID объявления") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        adService.deleteAd(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Мои объявления", description = "Возвращает объявления текущего пользователя")
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdResponse>> getMyAds(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(adService.getAllAdBySellerId(userDetails.getId()));
    }

    @Operation(
            summary = "Поиск объявлений",
            description = "Поиск с фильтрацией, сортировкой и пагинацией. " +
                    "Пример: /api/ads?title=iphone&minPrice=100&maxPrice=1000&sortField=price&sortDirection=ASC&page=0&size=20"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Результаты поиска")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<PagedResult<AdResponse>> searchAds(
            @RequestParam(required = false) @Parameter(description = "Поиск по названию") String title,
            @RequestParam(required = false) @Parameter(description = "Минимальная цена") BigDecimal minPrice,
            @RequestParam(required = false) @Parameter(description = "Максимальная цена") BigDecimal maxPrice,
            @RequestParam(required = false) @Parameter(description = "Статус объявления") AdStatus status,
            @RequestParam(required = false) @Parameter(description = "ID продавца") Long sellerId,
            @RequestParam(required = false) @Parameter(description = "Минимальный рейтинг продавца") Double minSellerRating,
            @RequestParam(defaultValue = "0") @Parameter(description = "Номер страницы") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Размер страницы") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "Поле сортировки") String sortField,
            @RequestParam(defaultValue = "DESC") @Parameter(description = "Направление сортировки (ASC/DESC)") String sortDirection) {

        Direction direction = Direction.valueOf(sortDirection.toUpperCase());
        Sort sort = Sort.by(sortField, direction);
        PagingRequest paging = new PagingRequest(page, size, sort);

        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle(title);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setStatus(status);
        criteria.setSellerId(sellerId);
        criteria.setMinSellerRating(minSellerRating);

        return ResponseEntity.ok(adService.searchAds(criteria, paging));
    }

    @Operation(
            summary = "Изменить статус объявления",
            description = "Возможные статусы: ACTIVE, SOLD, BOOKED"
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable @Parameter(description = "ID объявления") Long id,
            @RequestParam @Parameter(description = "Новый статус") AdStatus newStatus,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        adService.changeAdStatus(id, newStatus, userDetails);
        return ResponseEntity.ok().build();
    }
}