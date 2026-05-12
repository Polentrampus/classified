package com.classified.controller;

import com.classified.dto.user.UserResponse;
import com.classified.dto.user.UserUpdateRequest;
import com.classified.security.UserDetailsImpl;
import com.classified.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Управление профилем пользователя")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить свой профиль",
            description = "Возвращает профиль текущего аутентифицированного пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    @Operation(
            summary = "Обновить свой профиль",
            description = "Обновляет имя, фамилию, email и телефон текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
    }

    @Operation(
            summary = "Сменить пароль",
            description = "Меняет пароль текущего пользователя. Требует старый пароль для подтверждения"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль или пароли совпадают"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> changePassword(
            @RequestParam @Parameter(description = "Текущий пароль") String oldPassword,
            @RequestParam @Parameter(description = "Новый пароль") String newPassword,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.changePassword(userDetails.getId(), oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить свою статистику",
            description = "Статистика продаж: рейтинг, количество объявлений, продаж и общая выручка"
    )
    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyStatistics(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserStatistics(userDetails.getId()));
    }

    @Operation(
            summary = "Получить профиль пользователя по ID",
            description = "Просмотр профиля другого пользователя (например, продавца)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable @Parameter(description = "ID пользователя") Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }
}