package com.classified.controller;

import com.classified.dto.ad.AdResponse;
import com.classified.dto.image.AdImageRequest;
import com.classified.dto.image.AdImageResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdImageService;
import com.classified.service.AdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdImageController.class)
@Import(TestSecurityConfig.class)
class AdImageControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdImageService adImageService;

    @MockitoBean
    private AdService adService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createOwner() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("owner@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    private UserDetailsImpl createAdmin() {
        return new UserDetailsImpl(
                User.builder()
                        .id(2L)
                        .email("admin@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_ADMIN").build())
                        .build()
        );
    }

    private UserDetailsImpl createForeignUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(3L)
                        .email("foreign@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    private AdResponse createAdResponse(Long adId, Long sellerId) {
        return AdResponse.builder()
                .id(adId)
                .title("Test Ad")
                .sellerId(sellerId)
                .build();
    }

    @Test
    void shouldAddImagesAsOwner() throws Exception {
        Long adId = 10L;
        AdResponse ad = createAdResponse(adId, 1L);

        List<AdImageRequest> requests = List.of(
                AdImageRequest.builder().url("https://example.com/img1.jpg").isMain(true).build(),
                AdImageRequest.builder().url("https://example.com/img2.jpg").isMain(false).build()
        );

        List<AdImageResponse> responses = List.of(
                AdImageResponse.builder().id(1L).adId(adId).url("https://example.com/img1.jpg")
                        .isMain(true).createdAt(LocalDateTime.now()).build(),
                AdImageResponse.builder().id(2L).adId(adId).url("https://example.com/img2.jpg")
                        .isMain(false).createdAt(LocalDateTime.now()).build()
        );

        when(adService.getAd(adId)).thenReturn(ad);
        when(adImageService.addImages(eq(adId), anyList())).thenReturn(responses);

        mockMvc.perform(post("/api/ads/{adId}/images", adId)
                        .with(user(createOwner()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ads/10/images"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value("https://example.com/img1.jpg"))
                .andExpect(jsonPath("$[0].isMain").value(true))
                .andExpect(jsonPath("$[1].url").value("https://example.com/img2.jpg"))
                .andExpect(jsonPath("$[1].isMain").value(false));
    }

    @Test
    void shouldAddImagesAsAdmin() throws Exception {
        Long adId = 10L;
        AdResponse ad = createAdResponse(adId, 1L); // владелец id=1

        List<AdImageRequest> requests = List.of(
                AdImageRequest.builder().url("https://example.com/img.jpg").isMain(true).build()
        );

        List<AdImageResponse> responses = List.of(
                AdImageResponse.builder().id(1L).adId(adId).url("https://example.com/img.jpg")
                        .isMain(true).createdAt(LocalDateTime.now()).build()
        );

        when(adService.getAd(adId)).thenReturn(ad);
        when(adImageService.addImages(eq(adId), anyList())).thenReturn(responses);

        mockMvc.perform(post("/api/ads/{adId}/images", adId)
                        .with(user(createAdmin()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn403WhenForeignUserAddsImages() throws Exception {
        Long adId = 10L;
        AdResponse ad = createAdResponse(adId, 1L); // владелец id=1

        List<AdImageRequest> requests = List.of(
                AdImageRequest.builder().url("https://example.com/img.jpg").isMain(true).build()
        );

        when(adService.getAd(adId)).thenReturn(ad);

        mockMvc.perform(post("/api/ads/{adId}/images", adId)
                        .with(user(createForeignUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden());

        verify(adImageService, never()).addImages(anyLong(), anyList());
    }

    @Test
    void shouldGetImages() throws Exception {
        Long adId = 10L;
        List<AdImageResponse> responses = List.of(
                AdImageResponse.builder().id(1L).adId(adId).url("https://example.com/img.jpg")
                        .isMain(true).createdAt(LocalDateTime.now()).build()
        );

        when(adImageService.getImagesByAdId(adId)).thenReturn(responses);

        mockMvc.perform(get("/api/ads/{adId}/images", adId)
                        .with(user(createOwner()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].url").value("https://example.com/img.jpg"));
    }

    @Test
    void shouldUpdateImagesAsOwner() throws Exception {
        Long adId = 10L;
        AdResponse ad = createAdResponse(adId, 1L);

        List<AdImageRequest> requests = List.of(
                AdImageRequest.builder().url("https://example.com/new_img.jpg").isMain(true).build()
        );

        when(adService.getAd(adId)).thenReturn(ad);
        doNothing().when(adImageService).updateImages(eq(adId), anyList());

        mockMvc.perform(put("/api/ads/{adId}/images", adId)
                        .with(user(createOwner()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk());

        verify(adImageService).updateImages(eq(adId), anyList());
    }

    @Test
    void shouldReturn403WhenForeignUserUpdatesImages() throws Exception {
        Long adId = 10L;
        AdResponse ad = createAdResponse(adId, 1L);

        List<AdImageRequest> requests = List.of(
                AdImageRequest.builder().url("https://example.com/img.jpg").isMain(true).build()
        );

        when(adService.getAd(adId)).thenReturn(ad);

        mockMvc.perform(put("/api/ads/{adId}/images", adId)
                        .with(user(createForeignUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden());

        verify(adImageService, never()).updateImages(anyLong(), anyList());
    }

    @Test
    void shouldDeleteImageAsOwner() throws Exception {
        Long adId = 10L;
        Long imageId = 1L;
        AdResponse ad = createAdResponse(adId, 1L);

        when(adService.getAd(adId)).thenReturn(ad);
        doNothing().when(adImageService).deleteImage(imageId);

        mockMvc.perform(delete("/api/ads/{adId}/images/{imageId}", adId, imageId)
                        .with(user(createOwner()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(adImageService).deleteImage(imageId);
    }

    @Test
    void shouldReturn403WhenForeignUserDeletesImage() throws Exception {
        Long adId = 10L;
        Long imageId = 1L;
        AdResponse ad = createAdResponse(adId, 1L);

        when(adService.getAd(adId)).thenReturn(ad);

        mockMvc.perform(delete("/api/ads/{adId}/images/{imageId}", adId, imageId)
                        .with(user(createForeignUser()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(adImageService, never()).deleteImage(anyLong());
    }

}