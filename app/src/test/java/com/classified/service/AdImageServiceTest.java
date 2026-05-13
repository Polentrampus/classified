package com.classified.service;

import com.classified.dto.image.AdImageRequest;
import com.classified.dto.image.AdImageResponse;
import com.classified.entity.Ad;
import com.classified.entity.AdImage;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdImageMapper;
import com.classified.repository.AdImageRepository;
import com.classified.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdImageServiceTest {

    @Mock
    private AdImageRepository adImageRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private AdImageMapper adImageMapper;

    @InjectMocks
    private AdImageService adImageService;

    private Ad ad;
    private AdImageRequest imageRequest1;
    private AdImageRequest imageRequest2;
    private AdImage image1;
    private AdImage image2;
    private AdImageResponse imageResponse1;
    private AdImageResponse imageResponse2;

    @BeforeEach
    void setUp() {
        ad = Ad.builder()
                .id(10L)
                .title("Test Ad")
                .build();

        imageRequest1 = AdImageRequest.builder()
                .url("https://example.com/img1.jpg")
                .isMain(true)
                .build();

        imageRequest2 = AdImageRequest.builder()
                .url("https://example.com/img2.jpg")
                .isMain(false)
                .build();

        image1 = AdImage.builder()
                .id(1L)
                .ad(ad)
                .url("https://example.com/img1.jpg")
                .isMain(true)
                .createdAt(LocalDateTime.now())
                .build();

        image2 = AdImage.builder()
                .id(2L)
                .ad(ad)
                .url("https://example.com/img2.jpg")
                .isMain(false)
                .createdAt(LocalDateTime.now())
                .build();

        imageResponse1 = AdImageResponse.builder()
                .id(1L)
                .adId(10L)
                .url("https://example.com/img1.jpg")
                .isMain(true)
                .createdAt(LocalDateTime.now())
                .build();

        imageResponse2 = AdImageResponse.builder()
                .id(2L)
                .adId(10L)
                .url("https://example.com/img2.jpg")
                .isMain(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldAddImages() {
        List<AdImageRequest> requests = List.of(imageRequest1, imageRequest2);

        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adImageMapper.toEntity(imageRequest1)).thenReturn(image1);
        when(adImageMapper.toEntity(imageRequest2)).thenReturn(image2);
        when(adImageRepository.save(image1)).thenReturn(image1);
        when(adImageRepository.save(image2)).thenReturn(image2);
        when(adImageMapper.toResponse(image1)).thenReturn(imageResponse1);
        when(adImageMapper.toResponse(image2)).thenReturn(imageResponse2);

        List<AdImageResponse> result = adImageService.addImages(10L, requests);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdImageResponse::getUrl)
                .containsExactly("https://example.com/img1.jpg", "https://example.com/img2.jpg");
        assertThat(result).extracting(AdImageResponse::getIsMain)
                .containsExactly(true, false);

        verify(adImageMapper).toEntity(imageRequest1);
        verify(adImageMapper).toEntity(imageRequest2);
        verify(adImageRepository, times(2)).save(any(AdImage.class));
    }

    @Test
    void shouldAddEmptyImageList() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        List<AdImageResponse> result = adImageService.addImages(10L, List.of());

        assertThat(result).isEmpty();
        verify(adImageRepository, never()).save(any());
    }

    @Test
    void shouldGetImagesByAdId() {
        when(adImageRepository.findByAdId(10L)).thenReturn(List.of(image1, image2));
        when(adImageMapper.toResponseList(anyList())).thenReturn(List.of(imageResponse1, imageResponse2));

        List<AdImageResponse> result = adImageService.getImagesByAdId(10L);

        assertThat(result).hasSize(2);
        verify(adImageRepository).findByAdId(10L);
    }

    @Test
    void shouldReturnEmptyListWhenNoImagesForAd() {
        when(adImageRepository.findByAdId(10L)).thenReturn(List.of());
        when(adImageMapper.toResponseList(List.of())).thenReturn(List.of());

        List<AdImageResponse> result = adImageService.getImagesByAdId(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateImages() {
        List<AdImageRequest> requests = List.of(imageRequest1);

        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        doNothing().when(adImageRepository).deleteByAdId(10L);
        when(adImageMapper.toEntity(imageRequest1)).thenReturn(image1);
        when(adImageRepository.save(image1)).thenReturn(image1);

        adImageService.updateImages(10L, requests);

        verify(adImageRepository).deleteByAdId(10L);
        verify(adImageRepository).save(any(AdImage.class));
    }

    @Test
    void shouldUpdateImagesWithEmptyList() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        doNothing().when(adImageRepository).deleteByAdId(10L);

        adImageService.updateImages(10L, List.of());

        verify(adImageRepository).deleteByAdId(10L);
        verify(adImageRepository, never()).save(any());
    }

    @Test
    void shouldUpdateImagesWithNullList() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        doNothing().when(adImageRepository).deleteByAdId(10L);

        adImageService.updateImages(10L, null);

        verify(adImageRepository).deleteByAdId(10L);
        verify(adImageRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAdNotFoundForUpdateImages() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adImageService.updateImages(999L, List.of(imageRequest1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(adImageRepository, never()).deleteByAdId(anyLong());
        verify(adImageRepository, never()).save(any());
    }

    @Test
    void shouldDeleteImage() {
        when(adImageRepository.findById(1L)).thenReturn(Optional.of(image1));
        doNothing().when(adImageRepository).delete(image1);

        adImageService.deleteImage(1L);

        verify(adImageRepository).findById(1L);
        verify(adImageRepository).delete(image1);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentImage() {
        when(adImageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adImageService.deleteImage(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(adImageRepository, never()).delete(any());
    }

    @Test
    void shouldSetAdOnEachImageWhenAdding() {
        List<AdImageRequest> requests = List.of(imageRequest1, imageRequest2);

        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adImageMapper.toEntity(imageRequest1)).thenReturn(image1);
        when(adImageMapper.toEntity(imageRequest2)).thenReturn(image2);
        when(adImageRepository.save(any(AdImage.class))).thenReturn(image1, image2);
        when(adImageMapper.toResponse(any(AdImage.class))).thenReturn(imageResponse1, imageResponse2);

        adImageService.addImages(10L, requests);

        assertThat(image1.getAd()).isEqualTo(ad);
        assertThat(image2.getAd()).isEqualTo(ad);
    }

    @Test
    void shouldHandleSingleImageRequest() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adImageMapper.toEntity(imageRequest1)).thenReturn(image1);
        when(adImageRepository.save(image1)).thenReturn(image1);
        when(adImageMapper.toResponse(image1)).thenReturn(imageResponse1);

        List<AdImageResponse> result = adImageService.addImages(10L, List.of(imageRequest1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/img1.jpg");
        assertThat(result.get(0).getIsMain()).isTrue();
    }
}