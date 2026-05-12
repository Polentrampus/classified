package com.classified.controller;

import com.classified.dto.AuthResponse;
import com.classified.dto.LoginRequest;
import com.classified.dto.user.UserRegistrationRequest;
import com.classified.dto.user.UserResponse;
import com.classified.security.JwtService;
import com.classified.security.UserDetailsImpl;
import com.classified.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Регистрация и вход в систему")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по email и паролю, возвращает JWT токен"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getId(), userDetails.getUsername()));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя с ролью USER. Доступно без аутентификации" +
                    """
                            {
                              "name": "Иван",
                              "lastName": "Петров",
                              "email": "ivan@example.com",
                              "phone": "+79161234567",
                              "password": "password123"
                            }
                            """ )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "409", description = "Email или телефон уже занят")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @Operation(
            summary = "Создать администратора",
            description = "Создаёт пользователя с ролью ADMIN. Требует роль ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Администратор создан"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createAdmin(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.status(201).body(userService.createAdmin(request));
    }
}