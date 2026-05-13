package com.classified.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.classified.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",           // Swagger UI
            "/swagger-ui.html",         // Swagger UI HTML
            "/v3/api-docs/**",          // OpenAPI спецификация
            "/swagger-resources/**",    // Swagger ресурсы
            "/webjars/**"               // WebJars (css, js для Swagger)
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF — отключаем, т.к. REST API с JWT
                .csrf(csrf -> csrf.disable())

                // Сессии — не создаём (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Права доступа к URL
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/product-types/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/ad-types/**").authenticated()

                        // Только ADMIN
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")

                        // Все остальные — авторизованному пользователю
                        .anyRequest().authenticated())

                // Добавляем JWT-фильтр перед стандартным фильтром аутентификации
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}