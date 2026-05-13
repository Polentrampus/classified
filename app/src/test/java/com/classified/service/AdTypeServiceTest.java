package com.classified.service;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.dto.adType.AdTypeCreateRequest;
import com.classified.dto.adType.AdTypeResponse;
import com.classified.dto.adType.ProductTypeResponse;
import com.classified.entity.AdCategory;
import com.classified.entity.AdType;
import com.classified.entity.ProductType;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdTypeMapper;
import com.classified.repository.AdCategoryRepository;
import com.classified.repository.AdTypeRepository;
import com.classified.repository.ProductTypeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdTypeServiceTest {

    @Mock
    private AdTypeRepository adTypeRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private AdCategoryRepository adCategoryRepository;

    @Mock
    private AdTypeMapper adTypeMapper;

    @InjectMocks
    private AdTypeService adTypeService;

    private ProductType productType;
    private AdCategory category;
    private AdType adType;
    private AdTypeResponse adTypeResponse;
    private AdTypeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        productType = new ProductType();
        productType.setId(1L);
        productType.setName("Смартфоны");

        category = new AdCategory();
        category.setId(1L);
        category.setName("Электроника");

        adType = AdType.builder()
                .id(1L)
                .type(productType)
                .category(category)
                .build();

        adTypeResponse = AdTypeResponse.builder()
                .id(1L)
                .productType(ProductTypeResponse.builder().id(1L).name("Смартфоны").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        createRequest = AdTypeCreateRequest.builder()
                .productTypeId(1L)
                .categoryId(1L)
                .build();
    }

    @Test
    void shouldCreateAdType() {
        when(adTypeRepository.existsByTypeIdAndCategoryId(1L, 1L)).thenReturn(false);
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(adTypeRepository.save(any(AdType.class))).thenReturn(adType);
        when(adTypeMapper.toResponse(any(AdType.class))).thenReturn(adTypeResponse);

        AdTypeResponse result = adTypeService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductType().getName()).isEqualTo("Смартфоны");
        assertThat(result.getCategory().getName()).isEqualTo("Электроника");
        verify(adTypeRepository).existsByTypeIdAndCategoryId(1L, 1L);
        verify(adTypeRepository).save(any(AdType.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateAdType() {
        when(adTypeRepository.existsByTypeIdAndCategoryId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> adTypeService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessageContaining("Связь типа продукта и категории уже существует");

        verify(adTypeRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductTypeNotFound() {
        when(adTypeRepository.existsByTypeIdAndCategoryId(1L, 1L)).thenReturn(false);
        when(productTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adTypeService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductType")
                .hasMessageContaining("1");

        verify(adTypeRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(adTypeRepository.existsByTypeIdAndCategoryId(1L, 1L)).thenReturn(false);
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(adCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adTypeService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AdCategory")
                .hasMessageContaining("1");

        verify(adTypeRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAdType() {
        when(adTypeRepository.findById(1L)).thenReturn(Optional.of(adType));
        doNothing().when(adTypeRepository).delete(adType);

        adTypeService.delete(1L);

        verify(adTypeRepository).findById(1L);
        verify(adTypeRepository).delete(adType);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentAdType() {
        when(adTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adTypeService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(adTypeRepository, never()).delete(any());
    }

    @Test
    void shouldGetAdTypeById() {
        when(adTypeRepository.findById(1L)).thenReturn(Optional.of(adType));
        when(adTypeMapper.toResponse(adType)).thenReturn(adTypeResponse);

        AdTypeResponse result = adTypeService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductType().getName()).isEqualTo("Смартфоны");
        assertThat(result.getCategory().getName()).isEqualTo("Электроника");
    }

    @Test
    void shouldThrowExceptionWhenAdTypeNotFoundById() {
        when(adTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adTypeService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllAdTypes() {
        ProductType pt2 = new ProductType();
        pt2.setId(2L);
        pt2.setName("Ноутбуки");

        AdType adType2 = AdType.builder()
                .id(2L)
                .type(pt2)
                .category(category)
                .build();

        AdTypeResponse response2 = AdTypeResponse.builder()
                .id(2L)
                .productType(ProductTypeResponse.builder().id(2L).name("Ноутбуки").build())
                .category(AdCategoryResponse.builder().id(1L).name("Электроника").build())
                .build();

        when(adTypeRepository.findAll()).thenReturn(List.of(adType, adType2));
        when(adTypeMapper.toResponse(adType)).thenReturn(adTypeResponse);
        when(adTypeMapper.toResponse(adType2)).thenReturn(response2);

        List<AdTypeResponse> result = adTypeService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getProductType().getName())
                .containsExactly("Смартфоны", "Ноутбуки");
    }

    @Test
    void shouldReturnEmptyListWhenNoAdTypes() {
        when(adTypeRepository.findAll()).thenReturn(List.of());

        List<AdTypeResponse> result = adTypeService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAdTypesByCategoryId() {
        when(adTypeRepository.findByCategoryId(1L)).thenReturn(List.of(adType));
        when(adTypeMapper.toResponse(adType)).thenReturn(adTypeResponse);

        List<AdTypeResponse> result = adTypeService.getByCategoryId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Электроника");
        verify(adTypeRepository).findByCategoryId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenNoAdTypesForCategory() {
        when(adTypeRepository.findByCategoryId(999L)).thenReturn(List.of());

        List<AdTypeResponse> result = adTypeService.getByCategoryId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAdTypesByProductTypeId() {
        when(adTypeRepository.findByTypeId(1L)).thenReturn(List.of(adType));
        when(adTypeMapper.toResponse(adType)).thenReturn(adTypeResponse);

        List<AdTypeResponse> result = adTypeService.getByProductTypeId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductType().getName()).isEqualTo("Смартфоны");
        verify(adTypeRepository).findByTypeId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenNoAdTypesForProductType() {
        when(adTypeRepository.findByTypeId(999L)).thenReturn(List.of());

        List<AdTypeResponse> result = adTypeService.getByProductTypeId(999L);

        assertThat(result).isEmpty();
    }
}