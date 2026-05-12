package com.classified.controller;

import com.classified.dto.address.AddressCreateRequest;
import com.classified.dto.address.AddressResponse;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Создать адрес")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Адрес создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AddressResponse> create(
            @RequestBody @Valid AddressCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AddressResponse response = addressService.create(request, userDetails);
        return ResponseEntity
                .created(URI.create("/api/addresses/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Получить адрес по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    @Operation(summary = "Мои адреса")
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(addressService.getByUserId(userDetails.getId()));
    }

    @Operation(summary = "Адреса пользователя")
    @GetMapping("/byUser/{userId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getByUserId(userId));
    }

    @Operation(summary = "Адреса в городе")
    @GetMapping("/byCity/{cityId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getByCityId(@PathVariable Long cityId) {
        return ResponseEntity.ok(addressService.getByCityId(cityId));
    }

    @Operation(summary = "Удалить адрес")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Адрес удалён"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.delete(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}