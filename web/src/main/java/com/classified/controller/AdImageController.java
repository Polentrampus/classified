package com.classified.controller;

import com.classified.dto.image.AdImageRequest;
import com.classified.dto.image.AdImageResponse;
import com.classified.security.UserDetailsImpl;
import com.classified.service.AdImageService;
import com.classified.service.AdService;
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
@RequestMapping("/api/ads/{adId}/images")
@RequiredArgsConstructor
@Tag(name = "Ad Images", description = "Управление изображениями объявлений")
public class AdImageController {

    private final AdImageService adImageService;
    private final AdService adService;

    @Operation(summary = "Добавить изображения к объявлению")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Изображения добавлены"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdImageResponse>> addImages(
            @PathVariable Long adId,
            @RequestBody @Valid List<AdImageRequest> requests,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        for (AdImageRequest request : requests) {
            if (request.getUrl() == null || request.getUrl().isBlank()) {
                throw new com.classified.exception.business.BusinessException(
                        com.classified.exception.ErrorCode.VALIDATION_ERROR,
                        "URL изображения не может быть пустым"
                );
            }
        }
        var ad = adService.getAd(adId);
        boolean isOwner = ad.getSellerId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        List<AdImageResponse> responses = adImageService.addImages(adId, requests);
        return ResponseEntity.created(URI.create("/api/ads/" + adId + "/images")).body(responses);
    }

    @Operation(summary = "Получить изображения объявления")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<List<AdImageResponse>> getImages(@PathVariable Long adId) {
        adService.getAd(adId);
        return ResponseEntity.ok(adImageService.getImagesByAdId(adId));
    }

    @Operation(summary = "Обновить все изображения объявления")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> updateImages(
            @PathVariable Long adId,
            @RequestBody @Valid List<AdImageRequest> requests,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        var ad = adService.getAd(adId);
        boolean isOwner = ad.getSellerId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        adImageService.updateImages(adId, requests);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить конкретное изображение")
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long adId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        var ad = adService.getAd(adId);
        boolean isOwner = ad.getSellerId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        adImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}