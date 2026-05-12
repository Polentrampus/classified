package com.classified.controller;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.config.TestSecurityConfig;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdController.class)
@Import(TestSecurityConfig.class)
class AdControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdService adService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createTestUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("seller@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldCreateAd() throws Exception {
        UserDetailsImpl testUser = createTestUser();
        AdCreateRequest request = AdCreateRequest.builder()
                .title("Test Ad")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .build();

        AdResponse response = AdResponse.builder()
                .id(10L)
                .title("Test Ad")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .sellerId(1L)
                .build();

        when(adService.createAd(any(AdCreateRequest.class), any(UserDetailsImpl.class))).thenReturn(response);

        mockMvc.perform(post("/api/ads")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ads/10"))
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.sellerId").value(1));
    }

    @Test
    void shouldGetAd() throws Exception {
        AdResponse response = AdResponse.builder()
                .id(10L)
                .title("Test Ad")
                .price(new BigDecimal("100.00"))
                .sellerId(1L)
                .build();

        when(adService.getAd(10L)).thenReturn(response);

        mockMvc.perform(get("/api/ads/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.sellerId").value(1));
    }

    @Test
    void shouldDeleteAd() throws Exception {
        mockMvc.perform(delete("/api/ads/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetMyAds() throws Exception {
        AdResponse ad1 = AdResponse.builder().id(1L).title("Ad 1").sellerId(1L).build();
        AdResponse ad2 = AdResponse.builder().id(2L).title("Ad 2").sellerId(1L).build();

        when(adService.getAllAdBySellerId(1L)).thenReturn(List.of(ad1, ad2));

        mockMvc.perform(get("/api/ads/my")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Ad 1"));
    }
}