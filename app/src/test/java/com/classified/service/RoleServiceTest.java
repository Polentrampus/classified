package com.classified.service;

import com.classified.dto.role.RoleResponse;
import com.classified.entity.Role;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.RoleMapper;
import com.classified.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private RoleResponse roleResponse;
    private String roleName = "ROLE_MODERATOR";

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        roleResponse = RoleResponse.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
    }

    @Test
    void shouldCreateRole() {
        when(roleMapper.toEntity(roleName)).thenReturn(role);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(roleResponse);

        RoleResponse result = roleService.create(roleName);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_USER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldDeleteRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        roleService.delete(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRole() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(any(Role.class))).thenReturn(roleResponse);

        RoleResponse result = roleService.update(1L, "ROLE_ADMIN");

        assertThat(result).isNotNull();
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentRole() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.update(999L, "NEW_ROLE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.getRole(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRole(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllRoles() {
        Role adminRole = Role.builder().id(2L).name("ROLE_ADMIN").build();
        RoleResponse adminResponse = RoleResponse.builder().id(2L).name("ROLE_ADMIN").build();

        when(roleRepository.findAll()).thenReturn(List.of(role, adminRole));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);
        when(roleMapper.toResponse(adminRole)).thenReturn(adminResponse);

        List<RoleResponse> result = roleService.getAllRole();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RoleResponse::getName)
                .containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnEmptyListWhenNoRoles() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<RoleResponse> result = roleService.getAllRole();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetRoleByName() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.getRoleByName("ROLE_USER");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenRoleNameNotFound() {
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleByName("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}