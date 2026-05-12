package com.classified.repository;

import com.classified.entity.Order;
import com.classified.dto.OrderStatus;

import java.util.List;

public interface OrderRepository extends BaseRepository<Order, Long> {
    List<Order> findByAdId(Long adId);
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findBySellerId(Long sellerId);
    List<Order> findByStatus(OrderStatus status);
}
