package com.classified.controller;

import com.classified.dto.adType.AdTypeCreateRequest;
import com.classified.dto.adType.AdTypeResponse;
import com.classified.dto.adType.ProductTypeResponse;
import com.classified.dto.adType.AdCategoryResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdTypeController.class)
@Import(TestSecurityConfig.class)
class AdTypeControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdTypeService adTypeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void shouldCreateAdTypeAsAdmin() throws Exception {
        AdTypeCreateRequest request = AdTypeCreateRequest.builder()
                .productTypeId(1L)
                .categoryId(1L)
                .build();

        AdTypeResponse response = AdTypeResponse.builder()
                .id(1L)
                .productType(ProductTypeResponse.builder().id(1L).name("Смартфоны").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        when(adTypeService.create(any(AdTypeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/adTypes")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ad-types/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productType.id").value(1))
                .andExpect(jsonPath("$.productType.name").value("Смартфоны"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("Электроника"));
    }

    @Test
    void shouldReturn403WhenUserCreatesAdType() throws Exception {
        AdTypeCreateRequest request = AdTypeCreateRequest.builder()
                .productTypeId(1L)
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/adTypes")
                        .with(user(createUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(adTypeService, never()).create(any());
    }

    @Test
    void shouldGetAllAdTypes() throws Exception {
        AdTypeResponse at1 = AdTypeResponse.builder()
                .id(1L)
                .productType(ProductTypeResponse.builder().id(1L).name("Смартфоны").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();
        AdTypeResponse at2 = AdTypeResponse.builder()
                .id(2L)
                .productType(ProductTypeResponse.builder().id(2L).name("Ноутбуки").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        when(adTypeService.getAll()).thenReturn(List.of(at1, at2));

        mockMvc.perform(get("/api/adTypes")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productType.name").value("Смартфоны"))
                .andExpect(jsonPath("$[1].productType.name").value("Ноутбуки"));
    }

    @Test
    void shouldGetAdTypesByCategoryId() throws Exception {
        AdTypeResponse at1 = AdTypeResponse.builder()
                .id(1L)
                .productType(ProductTypeResponse.builder().id(1L).name("Смартфоны").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        when(adTypeService.getByCategoryId(1L)).thenReturn(List.of(at1));

        mockMvc.perform(get("/api/adTypes/byCategory/1")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category.name").value("Электроника"));
    }

    @Test
    void shouldGetAdTypesByProductTypeId() throws Exception {
        AdTypeResponse at1 = AdTypeResponse.builder()
                .id(1L)
                .productType(ProductTypeResponse.builder().id(1L).name("Смартфоны").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        when(adTypeService.getByProductTypeId(1L)).thenReturn(List.of(at1));

        mockMvc.perform(get("/api/adTypes/byProductType/1")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productType.name").value("Смартфоны"));
    }


    @Test
    void shouldReturn404WhenAdTypeNotFound() throws Exception {
        when(adTypeService.getById(999L))
                .thenThrow(new com.classified.exception.business.ResourceNotFoundException("AdType", "id", 999L));

        mockMvc.perform(get("/api/adTypes/999")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}