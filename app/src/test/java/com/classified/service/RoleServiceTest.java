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
        // given
        when(roleMapper.toEntity(roleName)).thenReturn(role);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(roleResponse);

        // when
        RoleResponse result = roleService.create(roleName);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_USER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldDeleteRole() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // when
        roleService.delete(1L);

        // then
        verify(roleRepository).delete(role);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRole() {
        // given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roleService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateRole() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(any(Role.class))).thenReturn(roleResponse);

        // when
        RoleResponse result = roleService.update(1L, "ROLE_ADMIN");

        // then
        assertThat(result).isNotNull();
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentRole() {
        // given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roleService.update(999L, "NEW_ROLE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetRole() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        // when
        RoleResponse result = roleService.getRole(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        // given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roleService.getRole(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllRoles() {
        // given
        Role adminRole = Role.builder().id(2L).name("ROLE_ADMIN").build();
        RoleResponse adminResponse = RoleResponse.builder().id(2L).name("ROLE_ADMIN").build();

        when(roleRepository.findAll()).thenReturn(List.of(role, adminRole));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);
        when(roleMapper.toResponse(adminRole)).thenReturn(adminResponse);

        // when
        List<RoleResponse> result = roleService.getAllRole();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RoleResponse::getName)
                .containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnEmptyListWhenNoRoles() {
        // given
        when(roleRepository.findAll()).thenReturn(List.of());

        // when
        List<RoleResponse> result = roleService.getAllRole();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetRoleByName() {
        // given
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        // when
        RoleResponse result = roleService.getRoleByName("ROLE_USER");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenRoleNameNotFound() {
        // given
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roleService.getRoleByName("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}