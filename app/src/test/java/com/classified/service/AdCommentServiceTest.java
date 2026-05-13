package com.classified.service;

import com.classified.dto.adComment.AdCommentCreateRequest;
import com.classified.dto.adComment.AdCommentResponse;
import com.classified.entity.*;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdCommentMapper;
import com.classified.repository.AdCommentRepository;
import com.classified.repository.OrderRepository;
import com.classified.dto.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdCommentServiceTest {

    @Mock
    private AdCommentRepository adCommentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AdCommentMapper adCommentMapper;

    @InjectMocks
    private AdCommentService adCommentService;

    private Order order;
    private AdCommentCreateRequest createRequest;
    private AdComment comment;
    private AdCommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(5L)
                .status(OrderStatus.COMPLETED)
                .build();

        createRequest = AdCommentCreateRequest.builder()
                .orderId(5L)
                .rating(5)
                .content("Excellent!")
                .build();

        comment = AdComment.builder()
                .id(1L)
                .order(order)
                .rating(5)
                .content("Excellent!")
                .build();

        commentResponse = AdCommentResponse.builder()
                .id(1L)
                .orderId(5L)
                .rating(5)
                .content("Excellent!")
                .build();
    }

    @Test
    void shouldCreateCommentForCompletedOrder() {
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(adCommentMapper.toEntity(any(AdCommentCreateRequest.class))).thenReturn(comment);
        when(adCommentRepository.save(any(AdComment.class))).thenReturn(comment);
        when(adCommentMapper.toResponse(any(AdComment.class))).thenReturn(commentResponse);

        AdCommentResponse result = adCommentService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("Excellent!");
        verify(adCommentRepository).save(any(AdComment.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotCompleted() {
        Order pendingOrder = Order.builder().id(5L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> adCommentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OPERATION)
                .hasMessageContaining("completed orders");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adCommentService.create(
                AdCommentCreateRequest.builder().orderId(999L).rating(3).content("Ok").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAverageRatingForUser() {
        when(adCommentRepository.getAverageRatingForUser(1L)).thenReturn(4.5);

        Double rating = adCommentService.getAverageRatingForUser(1L);

        assertThat(rating).isEqualTo(4.5);
    }

    @Test
    void shouldGetCommentsByAdId() {
        when(adCommentRepository.findByAdId(10L)).thenReturn(List.of(comment));
        when(adCommentMapper.toResponse(any(AdComment.class))).thenReturn(commentResponse);

        List<AdCommentResponse> result = adCommentService.getByAdId(10L);

        assertThat(result).hasSize(1);
    }
    @Test
    void shouldGetCommentsByAuthorId() {
        when(adCommentRepository.findByAuthorId(1L)).thenReturn(List.of(comment));
        when(adCommentMapper.toResponse(any(AdComment.class))).thenReturn(commentResponse);

        List<AdCommentResponse> result = adCommentService.getByAuthorId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetCommentsByTargetUserId() {
        when(adCommentRepository.findByTargetUserId(2L)).thenReturn(List.of(comment));
        when(adCommentMapper.toResponse(any(AdComment.class))).thenReturn(commentResponse);

        List<AdCommentResponse> result = adCommentService.getByTargetUserId(2L);
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetAverageRatingForAd() {
        when(adCommentRepository.getAverageRatingForAd(10L)).thenReturn(4.0);

        Double rating = adCommentService.getAverageRatingForAd(10L);
        assertThat(rating).isEqualTo(4.0);
    }
}