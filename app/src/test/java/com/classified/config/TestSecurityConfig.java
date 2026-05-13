package com.classified.config;

import com.classified.security.JwtService;
import com.classified.security.UserDetailsServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"code\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions\"}"
                            );
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"code\":\"UNAUTHORIZED\",\"message\":\"Full authentication is required\"}"
                            );
                        })
                )
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()

                        // GET доступен всем авторизованным
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/productTypes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/adTypes/**").authenticated()

                        // POST/PUT/DELETE только для ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/productTypes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productTypes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productTypes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/adTypes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/adTypes/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    @Primary
    public UserDetailsServiceImpl userDetailsService() {
        return mock(UserDetailsServiceImpl.class);
    }
}