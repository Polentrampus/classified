package com.classified.dto.order;

import com.classified.dto.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreateRequest {
    private Long adId;
    private Long buyerId;
    private Long sellerId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private OrderStatus status;
}
