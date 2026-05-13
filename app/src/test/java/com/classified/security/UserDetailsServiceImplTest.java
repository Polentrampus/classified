package com.classified.security;

import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldLoadUserByUsername() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.builder().name("ROLE_USER").build())
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}