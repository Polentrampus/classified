package com.classified.controller;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.dto.PromotionType;
import com.classified.security.UserDetailsImpl;
import com.classified.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Платное продвижение объявлений")
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "Купить продвижение", description = "Продвигает объявление в топ выдачи")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Промо активировано"),
            @ApiResponse(responseCode = "409", description = "Уже есть активное промо"),
            @ApiResponse(responseCode = "403", description = "Пользователь не владелец")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<PromotionResponse> create(
            @RequestBody @Valid PromotionCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PromotionResponse response = promotionService.createPromotion(request, userDetails);
        return ResponseEntity
                .created(URI.create("/api/promotions/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Статус продвижения объявления")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @GetMapping("/byAd/{adId}")
    public ResponseEntity<PromotionResponse> getActiveByAdId(@PathVariable Long adId) {
        PromotionResponse response = promotionService.getActiveByAdId(adId);
        if (response == null) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "Отменить продвижение", description = "Владелец или админ может отменить продвижение")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Промо деактивировано"),
            @ApiResponse(responseCode = "404", description = "Промо не найдено")
    })
    @DeleteMapping("/byAd/{adId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long adId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        promotionService.deactivatePromotion(adId, userDetails);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "Список типов продвижения")
    @GetMapping("/types")
    public ResponseEntity<PromotionType[]> getTypes() {
        return ResponseEntity.ok(PromotionType.values());
    }
}