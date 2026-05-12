package com.classified.controller;

import com.classified.dto.role.RoleResponse;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.config.TestSecurityConfig;
import com.classified.security.UserDetailsImpl;
import com.classified.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(TestSecurityConfig.class)
class RoleControllerTest extends BaseControllerTest {

    @MockitoBean
    private RoleService roleService;

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
    void shouldCreateRole() throws Exception {
        RoleResponse response = RoleResponse.builder()
                .id(1L)
                .name("ROLE_MODERATOR")
                .build();

        when(roleService.create("ROLE_MODERATOR")).thenReturn(response);

        mockMvc.perform(put("/api/roles")
                        .with(user(createAdmin()))
                        .with(csrf())
                        .param("name", "ROLE_MODERATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ROLE_MODERATOR"));
    }

    @Test
    void shouldGetRoleByName() throws Exception {
        RoleResponse response = RoleResponse.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        when(roleService.getRoleByName("ROLE_USER")).thenReturn(response);

        mockMvc.perform(get("/api/roles")
                        .with(user(createTestUser()))
                        .with(csrf())
                        .param("name", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ROLE_USER"));
    }
}