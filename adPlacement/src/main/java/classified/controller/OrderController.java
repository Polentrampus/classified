package classified.controller;

import classified.dto.order.OrderCreateRequest;
import classified.dto.order.OrderResponse;
import classified.entity.OrderStatus;
import classified.security.UserDetailsImpl;
import classified.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Управление заказами")
public class OrderController {

    private final OrderService orderService;

    /**
     * Создать заказ (покупка товара).
     * Покупателем автоматически становится текущий пользователь.
     * Продавец определяется из объявления.
     */
    @Operation(summary = "Создать заказ")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        request.setBuyerId(userDetails.getId()); // Покупатель — всегда текущий пользователь

        OrderResponse response = orderService.create(request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + response.getId()))
                .body(response);
    }

    /**
     * Получить заказ по ID
     */
    @Operation(summary = "Получить заказ по ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    /**
     * Мои покупки (где я покупатель)
     */
    @Operation(summary = "Мои покупки")
    @GetMapping("/my-buys")
    public ResponseEntity<List<OrderResponse>> getMyBuys(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getOrdersByBuyerId(userDetails.getId()));
    }

    /**
     * Мои продажи (где я продавец)
     */
    @Operation(summary = "Мои продажи")
    @GetMapping("/my-sales")
    public ResponseEntity<List<OrderResponse>> getMySales(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getOrdersBySellerId(userDetails.getId()));
    }

    /**
     * Изменить статус заказа (подтверждение, отправка, отмена)
     */
    @Operation(summary = "Изменить статус заказа")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus) {
        // В реальном проекте: загрузить заказ, проверить права,
        // проверить допустимость перехода статуса (state machine)
        orderService.setStatusOrderByAdId(id, newStatus);
        return ResponseEntity.ok().build();
    }
}