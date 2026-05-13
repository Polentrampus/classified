package com.classified.controller;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.dto.PromotionType;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.PromotionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
@Import(TestSecurityConfig.class)
class PromotionControllerTest extends BaseControllerTest {

    @MockitoBean
    private PromotionService promotionService;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreatePromotion() throws Exception {
        PromotionCreateRequest request = PromotionCreateRequest.builder()
                .adId(10L)
                .type(PromotionType.TOP_7_DAYS)
                .build();

        PromotionResponse response = PromotionResponse.builder()
                .id(1L)
                .adId(10L)
                .type(PromotionType.TOP_7_DAYS)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        when(promotionService.createPromotion(any(PromotionCreateRequest.class), any(UserDetailsImpl.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/promotions")
                        .with(user(createTestUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TOP_7_DAYS"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetActivePromotionByAdId() throws Exception {
        PromotionResponse response = PromotionResponse.builder()
                .id(1L)
                .adId(10L)
                .type(PromotionType.TOP_30_DAYS)
                .active(true)
                .build();

        when(promotionService.getActiveByAdId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/promotions/byAd/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TOP_30_DAYS"));
    }

    @Test
    void shouldDeactivatePromotion() throws Exception {
        mockMvc.perform(delete("/api/promotions/byAd/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPromotionTypes() throws Exception {
        mockMvc.perform(get("/api/promotions/types")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("TOP_7_DAYS"));
    }
}