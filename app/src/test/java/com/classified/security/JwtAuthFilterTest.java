package com.classified.security;

import com.classified.entity.Role;
import com.classified.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        UserDetailsImpl userDetails = new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email(email)
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWithoutAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenValidButUserDetailsExpired() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        UserDetailsImpl userDetails = new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email(email)
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAlreadyAuthenticatedRequest() throws ServletException, IOException {
        // эмулируем уже аутентифицированного пользователя
        UserDetailsImpl existingUser = new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("existing@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingUser, null, existingUser.getAuthorities())
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer some.token");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenAuthorizationHeaderHasWrongPrefix() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic someToken");

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).getEmailFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsEmpty() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}