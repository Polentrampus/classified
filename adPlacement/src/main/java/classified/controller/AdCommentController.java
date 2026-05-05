package classified.controller;

import classified.dto.adComment.AdCommentCreateRequest;
import classified.dto.adComment.AdCommentResponse;
import classified.entity.Ad;
import classified.entity.User;
import classified.exception.business.ResourceNotFoundException;
import classified.security.UserDetailsImpl;
import classified.service.AdCommentService;
import classified.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final OrderService orderService;

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
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AdCommentResponse> create(
            @RequestBody @Valid AdCommentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (orderService
                .getOrder(request
                        .getOrderId())
                .getBuyerId()
                .equals(userDetails.getId()) ||
                userDetails.
                        getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            AdCommentResponse response = adCommentService.create(request);
            return ResponseEntity
                    .created(URI.create("/api/comments/" + response.getId()))
                    .body(response);
        } else
            throw new AccessDeniedException("You can only edit your own ads");

    }

    /**
     * Получить все отзывы на конкретное объявление (через заказы)
     */
    @Operation(summary = "Отзывы по объявлению")
    @GetMapping("/by-ad/{adId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdCommentResponse>> getByAdId(@PathVariable Long adId) {
        return ResponseEntity.ok(adCommentService.getByAdId(adId));
    }

    /**
     * Получить все отзывы, оставленные пользователем (как покупателем)
     */
    @Operation(summary = "Отзывы, оставленные пользователем")
    @GetMapping("/by-author/{authorId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdCommentResponse>> getByAuthorId(@PathVariable Long authorId) {
        return ResponseEntity.ok(adCommentService.getByAuthorId(authorId));
    }

    /**
     * Получить все отзывы о продавце (целевой пользователь — продавец)
     */
    @Operation(summary = "Отзывы о продавце")
    @GetMapping("/by-target/{targetUserId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdCommentResponse>> getByTargetUserId(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(adCommentService.getByTargetUserId(targetUserId));
    }

    /**
     * Получить средний рейтинг продавца
     */
    @Operation(summary = "Средний рейтинг продавца")
    @GetMapping("/rating/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Double> getAverageRatingForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adCommentService.getAverageRatingForUser(userId));
    }

    /**
     * Получить средний рейтинг объявления
     */
    @Operation(summary = "Средний рейтинг объявления")
    @GetMapping("/rating/ad/{adId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Double> getAverageRatingForAd(@PathVariable Long adId) {
        return ResponseEntity.ok(adCommentService.getAverageRatingForAd(adId));
    }
}