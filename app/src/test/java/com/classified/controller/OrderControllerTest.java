package com.classified.controller;

import com.classified.dto.OrderStatus;
import com.classified.dto.order.OrderCreateRequest;
import com.classified.dto.order.OrderResponse;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.config.TestSecurityConfig;
import com.classified.security.UserDetailsImpl;
import com.classified.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(TestSecurityConfig.class)
class OrderControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createTestUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("buyer@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    private UserDetailsImpl createSecondUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(2L)
                        .email("seller@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    private OrderResponse createSampleOrderResponse() {
        return OrderResponse.builder()
                .id(100L)
                .adId(10L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .adId(10L)
                .sellerId(2L)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse response = createSampleOrderResponse();

        when(orderService.create(any(OrderCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.adId").value(10))
                .andExpect(jsonPath("$.buyerId").value(1))
                .andExpect(jsonPath("$.sellerId").value(2))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn401WhenCreatingOrderWithoutAuth() throws Exception {
        UserDetailsImpl testUser = createTestUser();
        OrderCreateRequest request = OrderCreateRequest.builder()
                .adId(10L)
                .sellerId(2L)
                .buyerId(testUser.getId())
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        OrderResponse response = createSampleOrderResponse();

        when(orderService.getOrder(100L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/100")
                        .with(user(createTestUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.adId").value(10))
                .andExpect(jsonPath("$.buyerId").value(1))
                .andExpect(jsonPath("$.sellerId").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.getOrder(999L))
                .thenThrow(new com.classified.exception.business.ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(get("/api/orders/999")
                        .with(user(createTestUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("999")));
    }

    @Test
    void shouldGetMyBuys() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        OrderResponse order1 = OrderResponse.builder()
                .id(100L)
                .adId(10L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse order2 = OrderResponse.builder()
                .id(101L)
                .adId(11L)
                .buyerId(1L)
                .sellerId(3L)
                .quantity(1)
                .totalPrice(new BigDecimal("500.00"))
                .status(OrderStatus.COMPLETED)
                .build();

        when(orderService.getOrdersByBuyerId(1L)).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/orders/myBuys")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].buyerId").value(1))
                .andExpect(jsonPath("$[1].buyerId").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void shouldReturnEmptyListWhenNoBuys() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        when(orderService.getOrdersByBuyerId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/myBuys")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetMySales() throws Exception {
        UserDetailsImpl sellerUser = createSecondUser();

        OrderResponse order1 = OrderResponse.builder()
                .id(200L)
                .adId(20L)
                .buyerId(1L)
                .sellerId(2L)
                .quantity(3)
                .totalPrice(new BigDecimal("300.00"))
                .status(OrderStatus.PAID)
                .build();

        OrderResponse order2 = OrderResponse.builder()
                .id(201L)
                .adId(21L)
                .buyerId(3L)
                .sellerId(2L)
                .quantity(1)
                .totalPrice(new BigDecimal("150.00"))
                .status(OrderStatus.SHIPPED)
                .build();

        when(orderService.getOrdersBySellerId(2L)).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/orders/mySales")
                        .with(user(sellerUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sellerId").value(2))
                .andExpect(jsonPath("$[1].sellerId").value(2))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].status").value("SHIPPED"));
    }

    @Test
    void shouldChangeOrderStatus() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        mockMvc.perform(patch("/api/orders/100/status")
                        .with(user(testUser))
                        .with(csrf())
                        .param("newStatus", "PAID"))
                .andExpect(status().isOk());

        verify(orderService).setStatusOrderByAdId(100L, OrderStatus.PAID, testUser);
    }

    @Test
    void shouldChangeOrderStatusToCompleted() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        mockMvc.perform(patch("/api/orders/100/status")
                        .with(user(testUser))
                        .with(csrf())
                        .param("newStatus", "COMPLETED"))
                .andExpect(status().isOk());

        verify(orderService).setStatusOrderByAdId(100L, OrderStatus.COMPLETED, testUser);
    }

    @Test
    void shouldChangeOrderStatusToCancelled() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        mockMvc.perform(patch("/api/orders/100/status")
                        .with(user(testUser))
                        .with(csrf())
                        .param("newStatus", "CANCELLED"))
                .andExpect(status().isOk());

        verify(orderService).setStatusOrderByAdId(100L, OrderStatus.CANCELLED, testUser);
    }

    @Test
    void shouldReturn401WhenChangingStatusWithoutAuth() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        mockMvc.perform(patch("/api/orders/100/status")
                        .with(csrf())
                        .param("newStatus", "PAID"))
                .andExpect(status().isUnauthorized());

        verify(orderService, org.mockito.Mockito.never())
                .setStatusOrderByAdId(100L, OrderStatus.PAID, testUser);
    }

    @Test
    void shouldVerifyBuyerIdIsSetToCurrentUser() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .adId(10L)
                .sellerId(2L)
                .quantity(1)
                .totalPrice(new BigDecimal("100.00"))
                .buyerId(999L)
                .build();

        OrderResponse response = createSampleOrderResponse();

        when(orderService.create(any(OrderCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<OrderCreateRequest> captor =
                org.mockito.ArgumentCaptor.forClass(OrderCreateRequest.class);
        verify(orderService).create(captor.capture());
        assertThat(captor.getValue().getBuyerId()).isEqualTo(1L);
    }
}