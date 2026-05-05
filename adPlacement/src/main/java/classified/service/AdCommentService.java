package classified.service;

import classified.dto.adComment.AdCommentCreateRequest;
import classified.dto.adComment.AdCommentResponse;
import classified.entity.AdComment;
import classified.entity.Order;
import classified.entity.OrderStatus;
import classified.entity.mappers.AdCommentMapper;
import classified.exception.ErrorCode;
import classified.exception.business.BusinessException;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AdCommentRepository;
import classified.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdCommentService {

    private final AdCommentRepository adCommentRepository;
    private final OrderRepository orderRepository;
    private final AdCommentMapper adCommentMapper;

    /**
     * Создать отзыв к заказу.
     * Один заказ — один отзыв, проверяется через уникальность order_id в БД.
     */
    @Transactional
    public AdCommentResponse create(AdCommentCreateRequest request) {
        // 1. Проверяем, что заказ существует и завершён
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Can only review completed orders");
        }

        AdComment comment = adCommentMapper.toEntity(request);
        comment.setOrder(order);

        return adCommentMapper.toResponse(adCommentRepository.save(comment));
    }

    /**
     * Получить средний рейтинг продавца
     */
    public Double getAverageRatingForUser(Long userId) {
        return adCommentRepository.getAverageRatingForUser(userId);
    }

    /**
     * Получить средний рейтинг объявления
     */
    public Double getAverageRatingForAd(Long adId) {
        return adCommentRepository.getAverageRatingForAd(adId);
    }

    /**
     * Получить все отзывы на объявления продавца (автор — покупатель)
     */
    public List<AdCommentResponse> getByAuthorId(Long authorId) {
        return adCommentRepository.findByAuthorId(authorId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
    }

    /**
     * Получить все отзывы о продавце (целевой пользователь — продавец)
     */
    public List<AdCommentResponse> getByTargetUserId(Long targetUserId) {
        return adCommentRepository.findByTargetUserId(targetUserId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
    }

    /**
     * Получить все отзывы по объявлению (через заказ)
     */
    public List<AdCommentResponse> getByAdId(Long adId) {
        return adCommentRepository.findByAdId(adId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
    }
}