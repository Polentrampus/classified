package classified.repository;

import classified.entity.Role;
import classified.entity.User;
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

//    @Test
//    public void shouldFindUserByEmail() {
//        User user = User.builder()
//                .name("Test")
//                .lastName("User")
//                .email("test@example.com")
//                .phone("+79161234567")
//                .password("a".repeat(61))
//                .role(userRole)
//                .build();
//        entityManager.persist(user);
//        entityManager.flush();
//        entityManager.clear();
//
//        Optional<User> found = userRepository.findByEmail("test@example.com");
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getName()).isEqualTo("Test");
//        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
//    }
//
//    @Test
//    public void shouldReturnEmptyWhenEmailNotFound() {
//        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    public void shouldCheckExistsByEmail() {
//        User user = User.builder()
//                .name("Test")
//                .lastName("User")
//                .email("exists@example.com")
//                .phone("+79161234568")
//                .password("a".repeat(61))
//                .role(userRole)
//                .build();
//        entityManager.persist(user);
//        entityManager.flush();
//
//        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
//        assertThat(userRepository.existsByEmail("no@example.com")).isFalse();
//    }
//
//    @Test
//    public void shouldCheckExistsByPhone() {
//        User user = User.builder()
//                .name("Test")
//                .lastName("User")
//                .email("phone@example.com")
//                .phone("+79161234569")
//                .password("a".repeat(61))
//                .role(userRole)
//                .build();
//        entityManager.persist(user);
//        entityManager.flush();
//
//        assertThat(userRepository.existsByPhone("+79161234569")).isTrue();
//        assertThat(userRepository.existsByPhone("+79999999999")).isFalse();
//    }
}
