package com.classified.service;

import com.classified.dto.order.OrderCreateRequest;
import com.classified.dto.order.OrderResponse;
import com.classified.dto.order.OrderUpdateRequest;
import com.classified.entity.Ad;
import com.classified.entity.Order;
import com.classified.dto.OrderStatus;
import com.classified.entity.User;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.OrderMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.classified.repository.OrderRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        log.info("Создание заказа: adId={}, buyerId={}, sellerId={}", request.getAdId(), request.getBuyerId(), request.getSellerId());
        log.debug("Детали: quantity={}, totalPrice={}, status={}", request.getQuantity(), request.getTotalPrice(), request.getStatus());

        Order order = orderMapper.toEntity(request);
        updateRelatedEntities(request.getAdId(), request.getBuyerId(), request.getSellerId(), order);
        OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
        log.info("Заказ создан: id={}, статус={}", response.getId(), response.getStatus());
        return response;
    }

    @Transactional
    public void delete(Long orderId) {
        log.info("Удаление заказа id={}", orderId);
        orderRepository.delete(orderRepository
                .findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Заказ с id={} не найден", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                }));
        log.info("Заказ id={} удалён", orderId);
    }

    @Transactional
    public OrderResponse update(OrderUpdateRequest request, Long orderId) {
        log.info("Обновление заказа id={}", orderId);
        log.debug("Новые данные: quantity={}, totalPrice={}, status={}", request.getQuantity(), request.getTotalPrice(), request.getStatus());

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Заказ с id={} не найден", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });

        orderMapper.updateEntityFromRequest(request, order);
        updateRelatedEntities(request.getAdId(), request.getBuyerId(), request.getSellerId(), order);
        OrderResponse response = orderMapper.toResponse(order);
        log.info("Заказ id={} обновлён", orderId);
        return response;
    }

    public OrderResponse getOrder(Long orderId) {
        log.debug("Запрос заказа id={}", orderId);
        OrderResponse response = orderMapper.toResponse(orderRepository
                .findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Заказ с id={} не найден", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                }));
        log.debug("Заказ найден: статус={}", response.getStatus());
        return response;
    }

    public List<OrderResponse> getAllOrder() {
        log.debug("Запрос всех заказов");
        List<OrderResponse> orders = orderRepository.findAll().stream()
                .map(orderMapper::toResponse).toList();
        log.debug("Найдено {} заказов", orders.size());
        return orders;
    }

    public List<OrderStatus> getStatusOrderByAdIdAndBuyerId(Long adId, Long buyerId) {
        log.debug("Запрос статусов заказов: adId={}, buyerId={}", adId, buyerId);
        List<OrderStatus> statuses = orderRepository.findByAdId(adId)
                .stream()
                .filter(order -> order.getBuyer().getId().equals(buyerId))
                .map(Order::getStatus)
                .toList();
        log.debug("Найдено {} статусов для adId={} и buyerId={}", statuses.size(), adId, buyerId);
        return statuses;
    }

    @Transactional
    public void setStatusOrderByAdId(Long adId, OrderStatus status, UserDetailsImpl userDetails) {
        log.info("Смена статуса заказа: adId={}, новый статус={}, userId={}", adId, status, userDetails.getId());

        Order order = orderRepository.findByAdId(adId)
                .stream()
                .max(Comparator.comparing(Order::getCompletedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> {
                    log.warn("Заказы для adId={} не найдены", adId);
                    return new ResourceNotFoundException("Order", "adId", adId);
                });
        boolean isSeller = order.getSeller().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSeller && !isAdmin) {
            log.warn("Отказ в доступе: userId={} не продавец (sellerId={}) и не админ",
                    userDetails.getId(), order.getSeller().getId());
            throw new BusinessException(ErrorCode.SECURITY_ERROR, "Only the seller or admin can change order status");
        }

        order.setStatus(status);
        log.info("Статус заказа для adId={} изменён на {}", adId, status);
    }

    public List<OrderResponse> getOrdersByAdId(Long adId) {
        log.debug("Запрос заказов по adId={}", adId);
        List<OrderResponse> orders = orderRepository
                .findByAdId(adId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        log.debug("Найдено {} заказов для adId={}", orders.size(), adId);
        return orders;
    }

    public List<OrderResponse> getOrdersByBuyerId(Long buyerId) {
        log.debug("Запрос заказов покупателя id={}", buyerId);
        List<OrderResponse> orders = orderRepository
                .findByBuyerId(buyerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        log.debug("Найдено {} заказов для покупателя id={}", orders.size(), buyerId);
        return orders;
    }

    public List<OrderResponse> getOrdersBySellerId(Long sellerId) {
        log.debug("Запрос заказов продавца id={}", sellerId);
        List<OrderResponse> orders = orderRepository
                .findBySellerId(sellerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        log.debug("Найдено {} заказов для продавца id={}", orders.size(), sellerId);
        return orders;
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.debug("Запрос заказов со статусом={}", status);
        List<OrderResponse> orders = orderRepository
                .findByStatus(status)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        log.debug("Найдено {} заказов со статусом={}", orders.size(), status);
        return orders;
    }

    private void updateRelatedEntities(Long adId, Long buyerId, Long sellerId, Order order) {
        log.debug("Обновление связей заказа: adId={}, buyerId={}, sellerId={}", adId, buyerId, sellerId);
        if (adId != null) {
            Ad ad = adRepository
                    .findById(adId)
                    .orElseThrow(() -> {
                        log.warn("Объявление с id={} не найдено", adId);
                        return new ResourceNotFoundException("Ad", "id", adId);
                    });
            order.setAd(ad);
        }
        if (buyerId != null) {
            User buyer = userRepository
                    .findById(buyerId)
                    .orElseThrow(() -> {
                        log.warn("Покупатель с id={} не найден", buyerId);
                        return new ResourceNotFoundException("User", "id", buyerId);
                    });
            order.setBuyer(buyer);
        }
        if (sellerId != null) {
            User seller = userRepository
                    .findById(sellerId)
                    .orElseThrow(() -> {
                        log.warn("Продавец с id={} не найден", sellerId);
                        return new ResourceNotFoundException("User", "id", sellerId);
                    });
            order.setSeller(seller);
        }
    }
}