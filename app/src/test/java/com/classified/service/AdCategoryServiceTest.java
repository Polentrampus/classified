package com.classified.service;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.entity.AdCategory;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdCategoryMapper;
import com.classified.repository.AdCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdCategoryServiceTest {

    @Mock
    private AdCategoryRepository adCategoryRepository;

    @Mock
    private AdCategoryMapper adCategoryMapper;

    @InjectMocks
    private AdCategoryService adCategoryService;

    private AdCategory category;
    private AdCategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = new AdCategory();
        category.setId(1L);
        category.setName("Электроника");

        categoryResponse = AdCategoryResponse.builder()
                .id(1L)
                .name("Электроника")
                .build();
    }

    @Test
    void shouldCreateCategory() {
        when(adCategoryRepository.existsByName("Электроника")).thenReturn(false);
        when(adCategoryMapper.toEntity("Электроника")).thenReturn(category);
        when(adCategoryRepository.save(any(AdCategory.class))).thenReturn(category);
        when(adCategoryMapper.toResponse(any(AdCategory.class))).thenReturn(categoryResponse);

        AdCategoryResponse result = adCategoryService.create("Электроника");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Электроника");
        verify(adCategoryRepository).existsByName("Электроника");
        verify(adCategoryRepository).save(any(AdCategory.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateCategory() {
        when(adCategoryRepository.existsByName("Электроника")).thenReturn(true);

        assertThatThrownBy(() -> adCategoryService.create("Электроника"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessageContaining("Электроника");

        verify(adCategoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategory() {
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(adCategoryRepository.existsByName("Новая Электроника")).thenReturn(false);
        when(adCategoryMapper.toResponse(any(AdCategory.class))).thenReturn(
                AdCategoryResponse.builder().id(1L).name("Новая Электроника").build());

        AdCategoryResponse result = adCategoryService.update(1L, "Новая Электроника");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новая Электроника");
        assertThat(category.getName()).isEqualTo("Новая Электроника");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(adCategoryRepository.existsByName("Одежда")).thenReturn(true);

        assertThatThrownBy(() -> adCategoryService.update(1L, "Одежда"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCategory() {
        when(adCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adCategoryService.update(999L, "Новая"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldDeleteCategory() {
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(adCategoryRepository).delete(category);

        adCategoryService.delete(1L);

        verify(adCategoryRepository).findById(1L);
        verify(adCategoryRepository).delete(category);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCategory() {
        when(adCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adCategoryService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(adCategoryRepository, never()).delete(any());
    }

    @Test
    void shouldGetCategoryById() {
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(adCategoryMapper.toResponse(category)).thenReturn(categoryResponse);

        AdCategoryResponse result = adCategoryService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Электроника");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundById() {
        when(adCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adCategoryService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllCategories() {
        AdCategory category2 = new AdCategory();
        category2.setId(2L);
        category2.setName("Одежда");

        AdCategoryResponse response2 = AdCategoryResponse.builder()
                .id(2L).name("Одежда").build();

        when(adCategoryRepository.findAll()).thenReturn(List.of(category, category2));
        when(adCategoryMapper.toResponse(category)).thenReturn(categoryResponse);
        when(adCategoryMapper.toResponse(category2)).thenReturn(response2);

        List<AdCategoryResponse> result = adCategoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdCategoryResponse::getName)
                .containsExactly("Электроника", "Одежда");
    }
}