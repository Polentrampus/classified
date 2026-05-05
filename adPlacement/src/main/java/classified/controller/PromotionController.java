package classified.controller;

import classified.dto.PromotionCreateRequest;
import classified.dto.PromotionResponse;
import classified.entity.PromotionType;
import classified.security.UserDetailsImpl;
import classified.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Платное продвижение объявлений")
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Создать промо для объявления (покупка продвижения).
     * Только владелец объявления может купить продвижение.
     */
    @Operation(summary = "Купить продвижение", description = "Продвигает объявление в топ выдачи")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Промо активировано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные или уже есть активное промо"),
            @ApiResponse(responseCode = "403", description = "Пользователь не владелец объявления"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    public ResponseEntity<PromotionResponse> create(
            @RequestBody @Valid PromotionCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PromotionResponse response = promotionService.createPromotion(request, userDetails);
        return ResponseEntity
                .created(URI.create("/api/promotions/" + response.getId()))
                .body(response);
    }

    /**
     * Проверить статус продвижения объявления
     */
    @Operation(summary = "Статус продвижения объявления")
    @GetMapping("/by-ad/{adId}")
    public ResponseEntity<PromotionResponse> getActiveByAdId(@PathVariable Long adId) {
        return ResponseEntity.ok(promotionService.getActiveByAdId(adId));
    }

    /**
     * Деактивировать промо (отменить продвижение)
     */
    @Operation(summary = "Отменить продвижение", description = "Владелец или админ может отменить продвижение")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Промо деактивировано"),
            @ApiResponse(responseCode = "404", description = "Промо не найдено")
    })
    @DeleteMapping("/by-ad/{adId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long adId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        promotionService.deactivatePromotion(adId, userDetails);
        return ResponseEntity.ok().build();
    }

    /**
     * Получить все типы промо (справочник)
     */
    @Operation(summary = "Справочник типов продвижения")
    @GetMapping("/types")
    public ResponseEntity<PromotionType[]> getTypes() {
        return ResponseEntity.ok(PromotionType.values());
    }
}