package com.classified.service;

import com.classified.dto.image.AdImageRequest;
import com.classified.dto.image.AdImageResponse;
import com.classified.entity.Ad;
import com.classified.entity.AdImage;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdImageMapper;
import com.classified.repository.AdImageRepository;
import com.classified.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdImageService {

    private final AdImageRepository adImageRepository;
    private final AdRepository adRepository;
    private final AdImageMapper adImageMapper;

    @Transactional
    public List<AdImageResponse> addImages(Long adId, List<AdImageRequest> imageRequests) {
        log.info("Добавление {} изображений для объявления id={}", imageRequests.size(), adId);

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });

        List<AdImageResponse> responses = new ArrayList<>();
        for (AdImageRequest request : imageRequests) {
            AdImage image = adImageMapper.toEntity(request);
            image.setAd(ad);
            AdImage saved = adImageRepository.save(image);
            responses.add(adImageMapper.toResponse(saved));
            log.debug("Добавлено изображение id={}, url={}", saved.getId(), saved.getUrl());
        }

        log.info("Добавлено {} изображений для объявления id={}", responses.size(), adId);
        return responses;
    }

    public List<AdImageResponse> getImagesByAdId(Long adId) {
        log.debug("Запрос изображений объявления id={}", adId);
        return adImageMapper.toResponseList(adImageRepository.findByAdId(adId));
    }

    @Transactional
    public void updateImages(Long adId, List<AdImageRequest> imageRequests) {
        log.info("Обновление изображений объявления id={}", adId);

        adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));

        adImageRepository.deleteByAdId(adId);

        if (imageRequests != null && !imageRequests.isEmpty()) {
            addImages(adId, imageRequests);
        }

        log.info("Изображения объявления id={} обновлены", adId);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Удаление изображения id={}", imageId);
        AdImage image = adImageRepository.findById(imageId)
                .orElseThrow(() -> {
                    log.warn("Изображение с id={} не найдено", imageId);
                    return new ResourceNotFoundException("AdImage", "id", imageId);
                });
        adImageRepository.delete(image);
        log.info("Изображение id={} удалено", imageId);
    }
}
