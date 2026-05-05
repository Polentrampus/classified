package classified.controller;

import classified.dto.ad.AdCreateRequest;
import classified.dto.ad.AdResponse;
import classified.dto.ad.AdSearchCriteria;
import classified.dto.ad.AdUpdateRequest;
import classified.entity.AdStatus;
import classified.security.UserDetailsImpl;
import classified.service.AdService;
import classified.util.pagination.Direction;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;
import classified.util.pagination.Sort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Ads", description = "Управление объявлениями")
public class AdController {

    private final AdService adService;

    /**
     * Создание нового объявления.
     * Продавцом автоматически становится текущий пользователь.
     */
    @Operation(summary = "Создать объявление")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Объявление создано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> createAd(@RequestBody @Valid AdCreateRequest request,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AdResponse response = adService.createAd(request, userDetails);
        return ResponseEntity
                .created(URI.create("/api/ads/" + response.getId()))
                .body(response);
    }

    /**
     * Обновление объявления.
     * Только владелец или админ может обновить.
     */
    @Operation(summary = "Обновить объявление")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> updateAd(
            @PathVariable Long id,
            @RequestBody @Valid AdUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(adService.updateAd(id, request, userDetails));
    }

    /**
     * Получить объявление по ID
     */
    @Operation(summary = "Получить объявление по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdResponse> getAd(@PathVariable Long id) {
        return ResponseEntity.ok(adService.getAd(id));
    }

    /**
     * Удалить объявление
     */
    @Operation(summary = "Удалить объявление")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        adService.deleteAd(id,userDetails);
        return ResponseEntity.noContent().build();
    }

    /**
     * Мои объявления (текущего пользователя как продавца)
     */
    @Operation(summary = "Мои объявления")
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdResponse>> getMyAds(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(adService.getAllAdBySellerId(userDetails.getId()));
    }

    /**
     * Поиск объявлений с фильтрацией, пагинацией и сортировкой.
     *
     * Пример запроса:
     * GET /api/v1/ads?title=iphone&minPrice=100&maxPrice=1000&sort=price&direction=ASC&page=0&size=20
     */
    @Operation(summary = "Поиск объявлений")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<PagedResult<AdResponse>> searchAds(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) AdStatus status,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Double minSellerRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        // Парсим пагинацию и сортировку
        Direction direction = Direction.valueOf(sortDirection.toUpperCase());
        Sort sort = Sort.by(sortField, direction);
        PagingRequest paging = new PagingRequest(page, size, sort);

        // Собираем критерии поиска
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle(title);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setStatus(status);
        criteria.setSellerId(sellerId);
        criteria.setMinSellerRating(minSellerRating);

        return ResponseEntity.ok(adService.searchAds(criteria, paging));
    }

    /**
     * Изменить статус объявления (ACTIVE → SOLD, BOOKED)
     */
    @Operation(summary = "Изменить статус объявления")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam AdStatus newStatus,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        adService.changeAdStatus(id, newStatus, userDetails);
        return ResponseEntity.ok().build();
    }
}