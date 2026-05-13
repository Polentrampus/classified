package com.classified.repository;

import com.classified.entity.Role;
import com.classified.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);
        entityManager.flush();
    }

    @Test
    public void shouldFindUserByEmail() {
        User user = User.builder()
                .name("Test")
                .lastName("User")
                .email("test@example.com")
                .phone("+79161234567")
                .password("a".repeat(61))
                .role(userRole)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistsByEmail() {
        User user = User.builder()
                .name("Test")
                .lastName("User")
                .email("exists@example.com")
                .phone("+79161234568")
                .password("a".repeat(61))
                .role(userRole)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("no@example.com")).isFalse();
    }

    @Test
    void shouldCheckExistsByPhone() {
        User user = User.builder()
                .name("Test")
                .lastName("User")
                .email("phone@example.com")
                .phone("+79161234569")
                .password("a".repeat(61))
                .role(userRole)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        assertThat(userRepository.existsByPhone("+79161234569")).isTrue();
        assertThat(userRepository.existsByPhone("+79999999999")).isFalse();
    }

    @Test
    void shouldSaveAndFindById() {
        User user = User.builder()
                .name("Save")
                .lastName("Test")
                .email("save@example.com")
                .phone("+79161234570")
                .password("a".repeat(61))
                .role(userRole)
                .build();

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("save@example.com");
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .name("Old")
                .lastName("Name")
                .email("update@example.com")
                .phone("+79161234571")
                .password("a".repeat(61))
                .role(userRole)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        user.setName("New");
        userRepository.update(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New");
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .name("Delete")
                .lastName("Test")
                .email("delete@example.com")
                .phone("+79161234572")
                .password("a".repeat(61))
                .role(userRole)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        Long id = user.getId();

        userRepository.delete(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
    }
}