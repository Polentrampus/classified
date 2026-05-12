package com.classified.service;

import com.classified.dto.adComment.AdCommentCreateRequest;
import com.classified.dto.adComment.AdCommentResponse;
import com.classified.entity.AdComment;
import com.classified.entity.Order;
import com.classified.dto.OrderStatus;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdCommentMapper;
import com.classified.repository.AdCommentRepository;
import com.classified.repository.OrderRepository;
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

    @Transactional
    public AdCommentResponse create(AdCommentCreateRequest request) {
        log.info("Создание отзыва для заказа id={}", request.getOrderId());
        log.debug("Детали запроса: rating={}, content={}", request.getRating(), request.getContent());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    log.warn("Заказ с id={} не найден", request.getOrderId());
                    return new ResourceNotFoundException("Order", "id", request.getOrderId());
                });

        if (order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("Попытка создать отзыв для незавершённого заказа id={}, статус={}",
                    request.getOrderId(), order.getStatus());
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Can only review completed orders");
        }

        AdComment comment = adCommentMapper.toEntity(request);
        comment.setOrder(order);

        AdCommentResponse response = adCommentMapper.toResponse(adCommentRepository.save(comment));
        log.info("Отзыв создан: id={}, orderId={}, rating={}", response.getId(), response.getOrderId(), response.getRating());
        return response;
    }

    public Double getAverageRatingForUser(Long userId) {
        log.debug("Запрос среднего рейтинга для userId={}", userId);
        Double rating = adCommentRepository.getAverageRatingForUser(userId);
        log.debug("Средний рейтинг для userId={}: {}", userId, rating);
        return rating;
    }

    public Double getAverageRatingForAd(Long adId) {
        log.debug("Запрос среднего рейтинга для adId={}", adId);
        Double rating = adCommentRepository.getAverageRatingForAd(adId);
        log.debug("Средний рейтинг для adId={}: {}", adId, rating);
        return rating;
    }

    public List<AdCommentResponse> getByAuthorId(Long authorId) {
        log.debug("Запрос отзывов автора id={}", authorId);
        List<AdCommentResponse> comments = adCommentRepository.findByAuthorId(authorId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
        log.debug("Найдено {} отзывов для автора id={}", comments.size(), authorId);
        return comments;
    }

    public List<AdCommentResponse> getByTargetUserId(Long targetUserId) {
        log.debug("Запрос отзывов о пользователе id={}", targetUserId);
        List<AdCommentResponse> comments = adCommentRepository.findByTargetUserId(targetUserId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
        log.debug("Найдено {} отзывов о пользователе id={}", comments.size(), targetUserId);
        return comments;
    }

    public List<AdCommentResponse> getByAdId(Long adId) {
        log.debug("Запрос отзывов для объявления id={}", adId);
        List<AdCommentResponse> comments = adCommentRepository.findByAdId(adId).stream()
                .map(adCommentMapper::toResponse)
                .toList();
        log.debug("Найдено {} отзывов для объявления id={}", comments.size(), adId);
        return comments;
    }
}