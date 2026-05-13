package com.classified.service;

import com.classified.dto.adType.AdTypeCreateRequest;
import com.classified.dto.adType.AdTypeResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdTypeService {

    private final AdTypeRepository adTypeRepository;
    private final ProductTypeRepository productTypeRepository;
    private final AdCategoryRepository adCategoryRepository;
    private final AdTypeMapper adTypeMapper;

    @Transactional
    public AdTypeResponse create(AdTypeCreateRequest request) {
        log.info("Создание связи типа объявления: productTypeId={}, categoryId={}",
                request.getProductTypeId(), request.getCategoryId());

        if (adTypeRepository.existsByTypeIdAndCategoryId(request.getProductTypeId(), request.getCategoryId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Связь типа продукта и категории уже существует");
        }

        ProductType productType = productTypeRepository.findById(request.getProductTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", "id", request.getProductTypeId()));

        AdCategory category = adCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("AdCategory", "id", request.getCategoryId()));

        AdType adType = AdType.builder()
                .type(productType)
                .category(category)
                .build();

        AdType saved = adTypeRepository.save(adType);
        log.info("Связь типа объявления создана: id={}", saved.getId());
        return adTypeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Удаление связи типа объявления id={}", id);
        AdType adType = adTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdType", "id", id));
        adTypeRepository.delete(adType);
        log.info("Связь типа объявления id={} удалена", id);
    }

    public AdTypeResponse getById(Long id) {
        return adTypeMapper.toResponse(
                adTypeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("AdType", "id", id)));
    }

    public List<AdTypeResponse> getAll() {
        return adTypeRepository.findAll().stream()
                .map(adTypeMapper::toResponse)
                .toList();
    }

    public List<AdTypeResponse> getByCategoryId(Long categoryId) {
        return adTypeRepository.findByCategoryId(categoryId).stream()
                .map(adTypeMapper::toResponse)
                .toList();
    }

    public List<AdTypeResponse> getByProductTypeId(Long productTypeId) {
        return adTypeRepository.findByTypeId(productTypeId).stream()
                .map(adTypeMapper::toResponse)
                .toList();
    }
}