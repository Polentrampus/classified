package com.classified.controller;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdCategoryController.class)
@Import(TestSecurityConfig.class)
class AdCategoryControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdCategoryService adCategoryService;

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

    private UserDetailsImpl createUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("user@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldCreateCategoryAsAdmin() throws Exception {
        AdCategoryResponse response = AdCategoryResponse.builder()
                .id(1L)
                .name("Электроника")
                .build();

        when(adCategoryService.create("Электроника")).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .param("name", "Электроника"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categories/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Электроника"));
    }

    @Test
    void shouldReturn403WhenUserCreatesCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .with(user(createUser()))
                        .with(csrf())
                        .param("name", "Электроника"))
                .andExpect(status().isForbidden());

        verify(adCategoryService, never()).create(anyString());
    }

    @Test
    void shouldUpdateCategoryAsAdmin() throws Exception {
        AdCategoryResponse response = AdCategoryResponse.builder()
                .id(1L)
                .name("Новая Электроника")
                .build();

        when(adCategoryService.update(eq(1L), eq("Новая Электроника"))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .param("name", "Новая Электроника"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Новая Электроника"));
    }

    @Test
    void shouldDeleteCategoryAsAdmin() throws Exception {
        doNothing().when(adCategoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1")
                        .with(user(createAdmin()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(adCategoryService).delete(1L);
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        AdCategoryResponse response = AdCategoryResponse.builder()
                .id(1L)
                .name("Электроника")
                .build();

        when(adCategoryService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/categories/1")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Электроника"));
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        AdCategoryResponse cat1 = AdCategoryResponse.builder().id(1L).name("Электроника").build();
        AdCategoryResponse cat2 = AdCategoryResponse.builder().id(2L).name("Одежда").build();

        when(adCategoryService.getAll()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Электроника"))
                .andExpect(jsonPath("$[1].name").value("Одежда"));
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        when(adCategoryService.getById(999L))
                .thenThrow(new com.classified.exception.business.ResourceNotFoundException("AdCategory", "id", 999L));

        mockMvc.perform(get("/api/categories/999")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}