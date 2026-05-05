package classified.service;

import classified.dto.order.OrderCreateRequest;
import classified.dto.order.OrderResponse;
import classified.dto.order.OrderUpdateRequest;
import classified.entity.Ad;
import classified.entity.AdType;
import classified.entity.Address;
import classified.entity.Order;
import classified.entity.OrderStatus;
import classified.entity.User;
import classified.entity.mappers.OrderMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AdRepository;
import classified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import classified.repository.OrderRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Order order = orderMapper.toEntity(request);
        updateRelatedEntities(request.getAdId(), request.getBuyerId(), request.getSellerId(), order);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public void delete(Long orderId) {
        orderRepository.delete(orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId)));
    }

    @Transactional
    public OrderResponse update(OrderUpdateRequest request, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        orderMapper.updateEntityFromRequest(request, order);
        updateRelatedEntities(request.getAdId(), request.getBuyerId(), request.getSellerId(), order);
        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrder(Long orderId) {
        return orderMapper.toResponse(orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId)));
    }

    public List<OrderResponse> getAllOrder() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    public List<OrderStatus> getStatusOrderByAdIdAndBuyerId(Long adId, Long buyerId) {
        return orderRepository.findByAdId(adId)
                .stream()
                .filter(order -> order.getBuyer().getId().equals(buyerId))
                .map(Order::getStatus)
                .toList();
    }
    @Transactional
    public void setStatusOrderByAdId(Long adId, OrderStatus status) {
        orderRepository.findByAdId(adId)
                .stream().max(Comparator.comparing(Order::getCompletedAt))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "adId", adId))
                .setStatus(status);
    }

    public List<OrderResponse> getOrdersByAdId(Long adId) {
        return orderRepository
                .findByAdId(adId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByBuyerId(Long buyerId) {
        return orderRepository
                .findByBuyerId(buyerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersBySellerId(Long sellerId) {
        return orderRepository
                .findBySellerId(sellerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository
                .findByStatus(status)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    private void updateRelatedEntities(Long adId, Long buyerId, Long sellerId, Order order) {
        if (adId != null) {
            Ad ad = adRepository
                    .findById(adId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
            order.setAd(ad);
        }
        if (buyerId != null) {
            User buyer = userRepository
                    .findById(buyerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));
            order.setBuyer(buyer);
        }
        if (sellerId != null) {
            User seller = userRepository
                    .findById(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", sellerId));
            order.setSeller(seller);
        }
    }
}
