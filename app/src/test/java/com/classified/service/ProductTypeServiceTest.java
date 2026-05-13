package com.classified.service;

import com.classified.dto.adType.ProductTypeResponse;
import com.classified.entity.ProductType;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.ProductTypeMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductTypeMapper productTypeMapper;

    @InjectMocks
    private ProductTypeService productTypeService;

    private ProductType productType;
    private ProductTypeResponse productTypeResponse;

    @BeforeEach
    void setUp() {
        productType = new ProductType();
        productType.setId(1L);
        productType.setName("Смартфоны");

        productTypeResponse = ProductTypeResponse.builder()
                .id(1L)
                .name("Смартфоны")
                .build();
    }

    @Test
    void shouldCreateProductType() {
        when(productTypeRepository.existsByName("Смартфоны")).thenReturn(false);
        when(productTypeMapper.toEntity("Смартфоны")).thenReturn(productType);
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);
        when(productTypeMapper.toResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        ProductTypeResponse result = productTypeService.create("Смартфоны");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Смартфоны");
        verify(productTypeRepository).existsByName("Смартфоны");
        verify(productTypeRepository).save(any(ProductType.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateProductType() {
        when(productTypeRepository.existsByName("Смартфоны")).thenReturn(true);

        assertThatThrownBy(() -> productTypeService.create("Смартфоны"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessageContaining("Смартфоны");

        verify(productTypeRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProductType() {
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(productTypeRepository.existsByName("Планшеты")).thenReturn(false);
        when(productTypeMapper.toResponse(any(ProductType.class))).thenReturn(
                ProductTypeResponse.builder().id(1L).name("Планшеты").build());

        ProductTypeResponse result = productTypeService.update(1L, "Планшеты");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Планшеты");
        assertThat(productType.getName()).isEqualTo("Планшеты");
    }

    @Test
    void shouldUpdateProductTypeWithSameName() {
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(productTypeMapper.toResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        ProductTypeResponse result = productTypeService.update(1L, "Смартфоны");

        assertThat(result).isNotNull();
        verify(productTypeRepository, never()).existsByName(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(productTypeRepository.existsByName("Ноутбуки")).thenReturn(true);

        assertThatThrownBy(() -> productTypeService.update(1L, "Ноутбуки"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProductType() {
        when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productTypeService.update(999L, "Новое"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldDeleteProductType() {
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        doNothing().when(productTypeRepository).delete(productType);

        productTypeService.delete(1L);

        verify(productTypeRepository).findById(1L);
        verify(productTypeRepository).delete(productType);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProductType() {
        when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productTypeService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(productTypeRepository, never()).delete(any());
    }

    @Test
    void shouldGetProductTypeById() {
        when(productTypeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(productTypeMapper.toResponse(productType)).thenReturn(productTypeResponse);

        ProductTypeResponse result = productTypeService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Смартфоны");
    }

    @Test
    void shouldThrowExceptionWhenProductTypeNotFoundById() {
        when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productTypeService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllProductTypes() {
        ProductType productType2 = new ProductType();
        productType2.setId(2L);
        productType2.setName("Ноутбуки");

        ProductTypeResponse response2 = ProductTypeResponse.builder()
                .id(2L).name("Ноутбуки").build();

        when(productTypeRepository.findAll()).thenReturn(List.of(productType, productType2));
        when(productTypeMapper.toResponse(productType)).thenReturn(productTypeResponse);
        when(productTypeMapper.toResponse(productType2)).thenReturn(response2);

        List<ProductTypeResponse> result = productTypeService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductTypeResponse::getName)
                .containsExactly("Смартфоны", "Ноутбуки");
    }

    @Test
    void shouldReturnEmptyListWhenNoProductTypes() {
        when(productTypeRepository.findAll()).thenReturn(List.of());

        List<ProductTypeResponse> result = productTypeService.getAll();

        assertThat(result).isEmpty();
    }
}