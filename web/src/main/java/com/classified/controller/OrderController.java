package com.classified.controller;

import com.classified.dto.OrderStatus;
import com.classified.dto.order.OrderCreateRequest;
import com.classified.dto.order.OrderResponse;
import com.classified.security.UserDetailsImpl;
import com.classified.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Создание и управление заказами")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Создать заказ", description = "Покупателем автоматически становится текущий пользователь" +
            """
                    {
                      "adId": 1,
                      "buyerId": 1,
                      "sellerId": 1,
                      "quantity": 1,
                      "totalPrice": 89990.00,
                      "status": "PENDING"
                    }
                    """)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        request.setBuyerId(userDetails.getId()); // Покупатель — всегда текущий пользователь

        OrderResponse response = orderService.create(request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Получить заказ по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @Operation(summary = "Мои покупки", description = "Заказы, где текущий пользователь — покупатель")
    @GetMapping("/myBuys")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMyBuys(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getOrdersByBuyerId(userDetails.getId()));
    }

    @Operation(summary = "Мои продажи", description = "Заказы, где текущий пользователь — продавец")
    @GetMapping("/mySales")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMySales(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getOrdersBySellerId(userDetails.getId()));
    }

    @Operation(summary = "Изменить статус заказа",
            description = "Возможные статусы: PENDING → PAID → SHIPPED → COMPLETED или CANCELLED")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.setStatusOrderByAdId(id, newStatus, userDetails);
        return ResponseEntity.ok().build();
    }
}