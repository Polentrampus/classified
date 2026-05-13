package com.classified.repository;

import com.classified.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .name("ROLE_USER")
                .build();
        entityManager.persist(userRole);

        adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        entityManager.persist(adminRole);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindRoleByName() {
        Optional<Role> found = roleRepository.findByName("ROLE_USER");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldReturnEmptyWhenRoleNotFoundByName() {
        Optional<Role> found = roleRepository.findByName("NONEXISTENT_ROLE");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveRole() {
        Role newRole = Role.builder()
                .name("ROLE_MODERATOR")
                .build();

        Role saved = roleRepository.save(newRole);
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_MODERATOR");
    }

    @Test
    void shouldUpdateRole() {
        String newName = "ROLE_UPDATED";

        userRole.setName(newName);
        roleRepository.update(userRole);
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findById(userRole.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(newName);
    }

    @Test
    void shouldDeleteRole() {
        roleRepository.delete(adminRole);
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findById(adminRole.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllRoles() {
        var roles = roleRepository.findAll();

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldCheckRoleExistsById() {
        assertThat(roleRepository.existsById(userRole.getId())).isTrue();
        assertThat(roleRepository.existsById(999L)).isFalse();
    }
}