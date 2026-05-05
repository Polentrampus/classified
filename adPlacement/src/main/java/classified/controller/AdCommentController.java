package classified.controller;

import classified.dto.adComment.AdCommentCreateRequest;
import classified.dto.adComment.AdCommentResponse;
import classified.security.UserDetailsImpl;
import classified.service.AdCommentService;
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
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Отзывы к заказам")
public class AdCommentController {

    private final AdCommentService adCommentService;

    /**
     * Создать отзыв к завершённому заказу.
     * Один заказ — один отзыв.
     */
    @Operation(summary = "Создать отзыв", description = "Покупатель оставляет отзыв к завершённому заказу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Отзыв создан"),
            @ApiResponse(responseCode = "400", description = "Заказ не завершён или дубликат"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    public ResponseEntity<AdCommentResponse> create(
            @RequestBody @Valid AdCommentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // В будущем: проверить, что userDetails.getId() — покупатель в заказе
        AdCommentResponse response = adCommentService.create(request);
        return ResponseEntity
                .created(URI.create("/api/comments/" + response.getId()))
                .body(response);
    }

    /**
     * Получить все отзывы на конкретное объявление (через заказы)
     */
    @Operation(summary = "Отзывы по объявлению")
    @GetMapping("/by-ad/{adId}")
    public ResponseEntity<List<AdCommentResponse>> getByAdId(@PathVariable Long adId) {
        return ResponseEntity.ok(adCommentService.getByAdId(adId));
    }

    /**
     * Получить все отзывы, оставленные пользователем (как покупателем)
     */
    @Operation(summary = "Отзывы, оставленные пользователем")
    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<AdCommentResponse>> getByAuthorId(@PathVariable Long authorId) {
        return ResponseEntity.ok(adCommentService.getByAuthorId(authorId));
    }

    /**
     * Получить все отзывы о продавце (целевой пользователь — продавец)
     */
    @Operation(summary = "Отзывы о продавце")
    @GetMapping("/by-target/{targetUserId}")
    public ResponseEntity<List<AdCommentResponse>> getByTargetUserId(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(adCommentService.getByTargetUserId(targetUserId));
    }

    /**
     * Получить средний рейтинг продавца
     */
    @Operation(summary = "Средний рейтинг продавца")
    @GetMapping("/rating/user/{userId}")
    public ResponseEntity<Double> getAverageRatingForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adCommentService.getAverageRatingForUser(userId));
    }

    /**
     * Получить средний рейтинг объявления
     */
    @Operation(summary = "Средний рейтинг объявления")
    @GetMapping("/rating/ad/{adId}")
    public ResponseEntity<Double> getAverageRatingForAd(@PathVariable Long adId) {
        return ResponseEntity.ok(adCommentService.getAverageRatingForAd(adId));
    }
}