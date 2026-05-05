package classified.controller;

import classified.dto.role.RoleResponse;
import classified.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "Управление ролями")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Добавить новую роль")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(
            @RequestParam String name) {
        return ResponseEntity.ok(roleService.create(name));
    }

    @Operation(summary = "Обновить роль")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(
            @RequestParam String name,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.update(roleId, name));
    }

    @Operation(summary = "Получить роль по name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль"),
            @ApiResponse(responseCode = "404", description = "Роль не найдена")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> getRoleByName(@RequestParam String name) {
        return ResponseEntity.ok(roleService.getRoleByName(name));
    }
}
