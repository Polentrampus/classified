package com.classified.controller;

import com.classified.dto.address.AddressCreateRequest;
import com.classified.dto.address.AddressResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@Import(TestSecurityConfig.class)
class AddressControllerTest extends BaseControllerTest {

    @MockitoBean
    private AddressService addressService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createTestUser() {
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
    void shouldCreateAddress() throws Exception {
        AddressCreateRequest request = AddressCreateRequest.builder()
                .cityId(5L)
                .build();

        AddressResponse response = AddressResponse.builder()
                .id(10L)
                .userId(1L)
                .cityId(5L)
                .build();

        when(addressService.create(any(AddressCreateRequest.class), any(UserDetailsImpl.class))).thenReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .with(user(createTestUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cityId").value(5));
    }

    @Test
    void shouldGetMyAddresses() throws Exception {
        AddressResponse addr1 = AddressResponse.builder().id(1L).userId(1L).cityId(5L).build();
        AddressResponse addr2 = AddressResponse.builder().id(2L).userId(1L).cityId(6L).build();

        when(addressService.getByUserId(1L)).thenReturn(List.of(addr1, addr2));

        mockMvc.perform(get("/api/addresses/my")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldDeleteAddress() throws Exception {
        mockMvc.perform(delete("/api/addresses/10")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}