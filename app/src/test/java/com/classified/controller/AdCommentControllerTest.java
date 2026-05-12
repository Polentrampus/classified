package com.classified.controller;

import com.classified.config.TestSecurityConfig;
import com.classified.dto.adComment.AdCommentCreateRequest;
import com.classified.dto.adComment.AdCommentResponse;
import com.classified.dto.order.OrderResponse;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdCommentService;
import com.classified.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdCommentController.class)
@Import(TestSecurityConfig.class)
class AdCommentControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdCommentService adCommentService;

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

    @Test
    void shouldCreateComment() throws Exception {
        UserDetailsImpl testUser = createTestUser();
        AdCommentCreateRequest request = AdCommentCreateRequest.builder()
                .orderId(5L)
                .rating(5)
                .content("Excellent!")
                .build();

        AdCommentResponse response = AdCommentResponse.builder()
                .id(1L)
                .orderId(5L)
                .rating(5)
                .content("Excellent!")
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(5L)
                .buyerId(1L)
                .build();

        when(orderService.getOrder(5L)).thenReturn(orderResponse);
        when(adCommentService.create(any(AdCommentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/comments")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("Excellent!"));
    }

    @Test
    void shouldGetCommentsByAdId() throws Exception {
        AdCommentResponse comment = AdCommentResponse.builder()
                .id(1L)
                .orderId(5L)
                .rating(4)
                .content("Good")
                .build();

        when(adCommentService.getByAdId(10L)).thenReturn(java.util.List.of(comment));

        mockMvc.perform(get("/api/comments/by-ad/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(4));
    }

    @Test
    void shouldGetAverageRatingForUser() throws Exception {
        when(adCommentService.getAverageRatingForUser(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/comments/rating/user/1")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }
}