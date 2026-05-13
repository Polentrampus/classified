package com.classified.service;

import com.classified.dto.adType.ProductTypeResponse;
import com.classified.entity.ProductType;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.ProductTypeMapper;
import com.classified.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final ProductTypeMapper productTypeMapper;

    @Transactional
    public ProductTypeResponse create(String name) {
        log.info("Создание нового типа продукта: {}", name);

        if (productTypeRepository.existsByName(name)) {
            log.warn("Тип продукта с именем '{}' уже существует", name);
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Тип продукта с именем '" + name + "' уже существует");
        }

        ProductType productType = productTypeMapper.toEntity(name);
        ProductType saved = productTypeRepository.save(productType);
        log.info("Тип продукта создан: id={}, name={}", saved.getId(), saved.getName());
        return productTypeMapper.toResponse(saved);
    }

    @Transactional
    public ProductTypeResponse update(Long id, String name) {
        log.info("Обновление типа продукта id={} на '{}'", id, name);

        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", "id", id));

        if (!productType.getName().equals(name) && productTypeRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Тип продукта с именем '" + name + "' уже существует");
        }

        productType.setName(name);
        log.info("Тип продукта id={} обновлён", id);
        return productTypeMapper.toResponse(productType);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Удаление типа продукта id={}", id);
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", "id", id));
        productTypeRepository.delete(productType);
        log.info("Тип продукта id={} удалён", id);
    }

    public ProductTypeResponse getById(Long id) {
        return productTypeMapper.toResponse(
                productTypeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ProductType", "id", id)));
    }

    public List<ProductTypeResponse> getAll() {
        return productTypeRepository.findAll().stream()
                .map(productTypeMapper::toResponse)
                .toList();
    }
}