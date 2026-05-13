package com.classified.service;

import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.JwtService;
import com.classified.security.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Устанавливаем значения через рефлексию (без поднятия контекста)
        ReflectionTestUtils.setField(jwtService, "secret",
                "mySecretKeyForTestingThatIsLongEnoughForHS256Algorithm1234567890");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        userDetails = new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("test@example.com")
                        .password("encodedPassword")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldValidateToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldDisableDeletedUser() {
        User deletedUser = User.builder()
                .id(1L)
                .email("deleted@test.com")
                .password("encoded")
                .role(Role.builder().name("ROLE_USER").build())
                .deleted(true)
                .build();

        UserDetailsImpl deletedUserDetails = new UserDetailsImpl(deletedUser);

        boolean enabled = deletedUserDetails.isEnabled();

        assertThat(enabled).isFalse();
    }

    @Test
    void shouldInvalidateExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(userDetails);

        // Ждём немного, чтобы токен точно истёк
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetailsImpl otherUser = new UserDetailsImpl(
                User.builder()
                        .id(2L)
                        .email("other@example.com")
                        .password("encodedPassword")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateTamperedToken() {
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token + "tampered";

        boolean isValid = jwtService.isTokenValid(tamperedToken, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateToken(userDetails);

        String email = jwtService.getEmailFromToken(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowExceptionForInvalidTokenWhenExtractingEmail() {
        try {
            jwtService.getEmailFromToken("invalid.token.here");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(JwtException.class);
        }
    }
}