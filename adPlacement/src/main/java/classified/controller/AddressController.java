package classified.controller;

import classified.dto.address.AddressCreateRequest;
import classified.dto.address.AddressResponse;
import classified.security.UserDetailsImpl;
import classified.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Управление адресами")
public class AddressController {

    private final AddressService addressService;

    /**
     * Создать новый адрес.
     * Привязывается к текущему пользователю.
     */
    @Operation(summary = "Создать адрес")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Адрес создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @RequestBody @Valid AddressCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Принудительно привязываем адрес к текущему пользователю
        request.setUserId(userDetails.getId());
        AddressResponse response = addressService.create(request);
        return ResponseEntity
                .created(URI.create("/api/addresses/" + response.getId()))
                .body(response);
    }

    /**
     * Получить адрес по ID
     */
    @Operation(summary = "Получить адрес по ID")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    /**
     * Получить все адреса текущего пользователя
     */
    @Operation(summary = "Мои адреса")
    @GetMapping("/my")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(addressService.getByUserId(userDetails.getId()));
    }

    /**
     * Получить все адреса пользователя по его ID
     */
    @Operation(summary = "Адреса пользователя")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<AddressResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getByUserId(userId));
    }

    /**
     * Получить все адреса в городе
     */
    @Operation(summary = "Адреса в городе")
    @GetMapping("/by-city/{cityId}")
    public ResponseEntity<List<AddressResponse>> getByCityId(@PathVariable Long cityId) {
        return ResponseEntity.ok(addressService.getByCityId(cityId));
    }

    /**
     * Удалить адрес
     */
    @Operation(summary = "Удалить адрес")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Адрес удалён"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}