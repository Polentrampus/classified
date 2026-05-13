package com.classified.service;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.entity.AdCategory;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdCategoryMapper;
import com.classified.repository.AdCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdCategoryService {

    private final AdCategoryRepository adCategoryRepository;
    private final AdCategoryMapper adCategoryMapper;

    @Transactional
    public AdCategoryResponse create(String name) {
        log.info("Создание новой категории: {}", name);

        if (adCategoryRepository.existsByName(name)) {
            log.warn("Категория с именем '{}' уже существует", name);
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Категория с именем '" + name + "' уже существует");
        }

        AdCategory category = adCategoryMapper.toEntity(name);
        AdCategory saved = adCategoryRepository.save(category);
        log.info("Категория создана: id={}, name={}", saved.getId(), saved.getName());
        return adCategoryMapper.toResponse(saved);
    }

    @Transactional
    public AdCategoryResponse update(Long id, String name) {
        log.info("Обновление категории id={} на '{}'", id, name);

        AdCategory category = adCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdCategory", "id", id));

        if (!category.getName().equals(name) && adCategoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Категория с именем '" + name + "' уже существует");
        }

        category.setName(name);
        log.info("Категория id={} обновлена", id);
        return adCategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Удаление категории id={}", id);
        AdCategory category = adCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdCategory", "id", id));
        adCategoryRepository.delete(category);
        log.info("Категория id={} удалена", id);
    }

    public AdCategoryResponse getById(Long id) {
        return adCategoryMapper.toResponse(
                adCategoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("AdCategory", "id", id)));
    }

    public List<AdCategoryResponse> getAll() {
        return adCategoryRepository.findAll().stream()
                .map(adCategoryMapper::toResponse)
                .toList();
    }
}