package classified.controller;

import classified.dto.user.UserResponse;
import classified.dto.user.UserUpdateRequest;
import classified.security.UserDetailsImpl;
import classified.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление профилем пользователя")
public class UserController {

    private final UserService userService;

    /**
     * Получить профиль текущего пользователя
     * GET /api/users/me
     */
    @Operation(summary = "Получить свой профиль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    /**
     * Обновить профиль текущего пользователя
     * PUT /api/users/me
     */
    @Operation(summary = "Обновить свой профиль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
    }

    /**
     * Сменить пароль
     * PATCH /api/users/me/password
     *
     * PATCH, а не PUT, потому что меняем только одно поле, а не весь ресурс
     */
    @Operation(summary = "Сменить пароль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.changePassword(userDetails.getId(), oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    /**
     * Получить статистику текущего пользователя (продажи, рейтинг, выручка)
     * GET /api/users/me/statistics
     */
    @Operation(summary = "Получить свою статистику")
    @GetMapping("/me/statistics")
    public ResponseEntity<?> getMyStatistics(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserStatistics(userDetails.getId()));
    }

    /**
     * Получить профиль другого пользователя по ID (для просмотра продавца)
     * GET /api/users/{id}
     */
    @Operation(summary = "Получить профиль пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }
}