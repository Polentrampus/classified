package com.classified.controller;

import com.classified.dto.user.UserResponse;
import com.classified.dto.user.UserStatisticsDto;
import com.classified.dto.user.UserUpdateRequest;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserService userService;

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

    private UserDetailsImpl createAdmin() {
        return new UserDetailsImpl(
                User.builder()
                        .id(2L)
                        .email("seller@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_ADMIN").build())
                        .build()
        );
    }
    @Test
    void shouldGetMyProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .email("test@example.com")
                .role("ROLE_USER")
                .rating(BigDecimal.ZERO)
                .build();

        when(userService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateMyProfile() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Jane")
                .lastName("Doe")
                .email("buyer@test.com")
                .phone("+79169876543")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .name("Jane")
                .lastName("Doe")
                .email("buyer@test.com")
                .phone("+79169876543")
                .role("ROLE_USER")
                .build();

        when(userService.updateProfile(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/me")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("buyer@test.com"));
    }

    @Test
    void shouldChangePassword() throws Exception {
        UserDetailsImpl testUser = createTestUser();
        doNothing().when(userService).changePassword(1L, "oldPass", "newPass");

        mockMvc.perform(patch("/api/users/me/password")
                        .with(user(testUser))
                        .param("oldPassword", "oldPass")
                        .param("newPassword", "newPass"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMyStatistics() throws Exception {
        UserDetailsImpl testUser = createTestUser();

        UserStatisticsDto stats = new UserStatisticsDto(
                1L, "John", new BigDecimal("4.50"), 10L, 5L, new BigDecimal("5000.00"));

        when(userService.getUserStatistics(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/users/me/statistics")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.50))
                .andExpect(jsonPath("$.totalAds").value(10))
                .andExpect(jsonPath("$.totalSales").value(5));
    }

    @Test
    void shouldGetUserById() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(2L)
                .name("Seller")
                .lastName("Test")
                .email("seller@example.com")
                .role("ROLE_USER")
                .rating(new BigDecimal("3.50"))
                .build();

        when(userService.getProfile(2L)).thenReturn(response);

        mockMvc.perform(get("/api/users/2")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rating").value(3.50));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getProfile(999L))
                .thenThrow(new com.classified.exception.business.ResourceNotFoundException("User", "id", 999L));

        MvcResult result = mockMvc.perform(get("/api/users/999")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("RESOURCE_NOT_FOUND");
        assertThat(content).contains("999");
    }
}