package com.classified.controller;

import com.classified.dto.adType.ProductTypeResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.ProductTypeService;
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

@WebMvcTest(ProductTypeController.class)
@Import(TestSecurityConfig.class)
class ProductTypeControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProductTypeService productTypeService;

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
    void shouldCreateProductTypeAsAdmin() throws Exception {
        ProductTypeResponse response = ProductTypeResponse.builder()
                .id(1L)
                .name("Смартфоны")
                .build();

        when(productTypeService.create("Смартфоны")).thenReturn(response);

        mockMvc.perform(post("/api/productTypes")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .param("name", "Смартфоны"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/product-types/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Смартфоны"));
    }

    @Test
    void shouldReturn403WhenUserCreatesProductType() throws Exception {
        mockMvc.perform(post("/api/productTypes")
                        .with(user(createUser()))
                        .with(csrf())
                        .param("name", "Смартфоны"))
                .andExpect(status().isForbidden());

        verify(productTypeService, never()).create(anyString());
    }

    @Test
    void shouldUpdateProductTypeAsAdmin() throws Exception {
        ProductTypeResponse response = ProductTypeResponse.builder()
                .id(1L)
                .name("Планшеты")
                .build();

        when(productTypeService.update(eq(1L), eq("Планшеты"))).thenReturn(response);

        mockMvc.perform(put("/api/productTypes/1")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .param("name", "Планшеты"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Планшеты"));
    }

    @Test
    void shouldDeleteProductTypeAsAdmin() throws Exception {
        doNothing().when(productTypeService).delete(1L);

        mockMvc.perform(delete("/api/productTypes/1")
                        .with(user(createAdmin()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productTypeService).delete(1L);
    }

    @Test
    void shouldGetProductTypeById() throws Exception {
        ProductTypeResponse response = ProductTypeResponse.builder()
                .id(1L)
                .name("Смартфоны")
                .build();

        when(productTypeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/productTypes/1")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Смартфоны"));
    }

    @Test
    void shouldGetAllProductTypes() throws Exception {
        ProductTypeResponse pt1 = ProductTypeResponse.builder().id(1L).name("Смартфоны").build();
        ProductTypeResponse pt2 = ProductTypeResponse.builder().id(2L).name("Ноутбуки").build();

        when(productTypeService.getAll()).thenReturn(List.of(pt1, pt2));

        mockMvc.perform(get("/api/productTypes")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Смартфоны"))
                .andExpect(jsonPath("$[1].name").value("Ноутбуки"));
    }

    @Test
    void shouldReturn404WhenProductTypeNotFound() throws Exception {
        when(productTypeService.getById(999L))
                .thenThrow(new com.classified.exception.business.ResourceNotFoundException("ProductType", "id", 999L));

        mockMvc.perform(get("/api/productTypes/999")
                        .with(user(createUser()))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}