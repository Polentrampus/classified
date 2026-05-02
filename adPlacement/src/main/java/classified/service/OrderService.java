package classified.service;

import classified.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import classified.repository.OrderRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderStatus checkStatusAd(Long adId) {
        return orderRepository.findAll()
                .stream()
                .filter(order -> order
                        .getAd()
                        .getId()
                        .equals(adId))
                .findFirst()
                .orElseThrow()
                .getStatus();
    }

    public void setStatusAd(Long adId, OrderStatus status) {
        orderRepository.findAll()
                .stream()
                .filter(order -> order
                        .getAd()
                        .getId()
                        .equals(adId))
                .findFirst()
                .orElseThrow()
                .setStatus(status);
    }
}
