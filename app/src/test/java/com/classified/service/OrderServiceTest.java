package com.classified.service;

import com.classified.dto.OrderStatus;
import com.classified.dto.order.OrderCreateRequest;
import com.classified.dto.order.OrderResponse;
import com.classified.dto.order.OrderUpdateRequest;
import com.classified.entity.Ad;
import com.classified.entity.Order;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.OrderMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.OrderRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateRequest createRequest;
    private OrderUpdateRequest updateRequest;
    private Order order;
    private OrderResponse orderResponse;
    private Ad ad;
    private User buyer;
    private User seller;

    @BeforeEach
    void setUp() {
        buyer = User.builder().id(1L).email("buyer@test.com").build();
        seller = User.builder().id(2L).email("seller@test.com").build();

        ad = Ad.builder()
                .id(10L)
                .title("Test Ad")
                .seller(seller)
                .build();

        createRequest = OrderCreateRequest.builder()
                .adId(10L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();

        updateRequest = OrderUpdateRequest.builder()
                .adId(10L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(3)
                .totalPrice(new BigDecimal("300.00"))
                .status(OrderStatus.PAID)
                .build();

        order = Order.builder()
                .id(100L)
                .ad(ad)
                .buyer(buyer)
                .seller(seller)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();

        orderResponse = OrderResponse.builder()
                .id(100L)
                .adId(10L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    void shouldCreateOrder() {
        when(orderMapper.toEntity(any(OrderCreateRequest.class))).thenReturn(order);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getQuantity()).isEqualTo(2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenAdNotFoundForOrder() {
        when(orderMapper.toEntity(any(OrderCreateRequest.class))).thenReturn(order);
        when(adRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void shouldThrowExceptionWhenBuyerNotFound() {
        when(orderMapper.toEntity(any(OrderCreateRequest.class))).thenReturn(order);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void shouldDeleteOrder() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.delete(100L);

        verify(orderRepository).delete(order);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateOrder() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        doNothing().when(orderMapper).updateEntityFromRequest(any(), any());
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.update(updateRequest, 100L);

        assertThat(result).isNotNull();
        verify(orderMapper).updateEntityFromRequest(updateRequest, order);
    }

    @Test
    void shouldGetOrder() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrder(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void shouldGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getAllOrder();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetOrdersByAdId() {
        when(orderRepository.findByAdId(10L)).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByAdId(10L);

        assertThat(result).hasSize(1);
        verify(orderRepository).findByAdId(10L);
    }

    @Test
    void shouldGetOrdersByBuyerId() {
        when(orderRepository.findByBuyerId(1L)).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByBuyerId(1L);

        assertThat(result).hasSize(1);
        verify(orderRepository).findByBuyerId(1L);
    }

    @Test
    void shouldGetOrdersBySellerId() {
        when(orderRepository.findBySellerId(2L)).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersBySellerId(2L);

        assertThat(result).hasSize(1);
        verify(orderRepository).findBySellerId(2L);
    }

    @Test
    void shouldGetOrdersByStatus() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertThat(result).hasSize(1);
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void shouldGetStatusOrderByAdIdAndBuyerId() {
        when(orderRepository.findByAdId(10L)).thenReturn(List.of(order));

        List<OrderStatus> result = orderService.getStatusOrderByAdIdAndBuyerId(10L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void shouldReturnEmptyListWhenBuyerNotMatchInGetStatusOrder() {
        when(orderRepository.findByAdId(10L)).thenReturn(List.of(order));

        List<OrderStatus> result = orderService.getStatusOrderByAdIdAndBuyerId(10L, 999L);

        assertThat(result).isEmpty();
    }

    private UserDetailsImpl createTestUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(2L)
                        .email("buyer@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldSetStatusOrderByAdId() {
        UserDetailsImpl testUser = createTestUser();

        Order completedOrder = Order.builder()
                .id(100L)
                .ad(ad)
                .buyer(buyer)
                .seller(seller)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .completedAt(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findByAdId(10L)).thenReturn(List.of(completedOrder));

        orderService.setStatusOrderByAdId(10L, OrderStatus.COMPLETED, testUser);

        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldThrowExceptionWhenNoOrdersForAdId() {
        UserDetailsImpl testUser = createTestUser();

        when(orderRepository.findByAdId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.setStatusOrderByAdId(10L, OrderStatus.COMPLETED, testUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}