package com.classified.service;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.entity.Ad;
import com.classified.entity.Promotion;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.PromotionMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.PromotionRepository;
import com.classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final AdRepository adRepository;
    private final PromotionMapper promotionMapper;

    @Transactional
    public PromotionResponse createPromotion(PromotionCreateRequest request, UserDetailsImpl userDetails) {
        log.info("Создание промо: adId={}, тип={}, userId={}", request.getAdId(), request.getType(), userDetails.getId());

        Ad ad = adRepository.findById(request.getAdId())
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", request.getAdId());
                    return new ResourceNotFoundException("Ad", "id", request.getAdId());
                });

        if (!ad.getSeller().getId().equals(userDetails.getId())) {
            log.warn("Отказ в доступе: userId={} не владелец объявления adId={}, владелец={}",
                    userDetails.getId(), request.getAdId(), ad.getSeller().getId());
            throw new AccessDeniedException("Only the owner can promote their ad");
        }

        promotionRepository.findActiveByAdId(request.getAdId()).ifPresent(p -> {
            log.warn("Для adId={} уже есть активное промо до {}", request.getAdId(), p.getEndDate());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Ad already has active promotion until " + p.getEndDate());
        });

        int days = switch (request.getType()) {
            case TOP_7_DAYS -> 7;
            case TOP_30_DAYS -> 30;
            case HIGHLIGHT -> 7;
        };

        Promotion promotion = new Promotion();
        promotion.setAd(ad);
        promotion.setType(request.getType());
        promotion.setStartDate(LocalDateTime.now());
        promotion.setEndDate(LocalDateTime.now().plusDays(days));
        promotion.setActive(true);

        Promotion saved = promotionRepository.save(promotion);
        PromotionResponse response = promotionMapper.toResponse(saved);
        log.info("Промо создано: id={}, adId={}, тип={}, до {}", response.getId(), response.getAdId(), response.getType(), response.getEndDate());
        return response;
    }

    public PromotionResponse getActiveByAdId(Long adId) {
        log.debug("Запрос активного промо для adId={}", adId);
        adRepository.findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });

        Promotion active = promotionRepository.findActiveByAdId(adId)
                .orElseThrow(() -> {
                    log.warn("Активное промо для adId={} не найдено", adId);
                    return new ResourceNotFoundException("Promotion", "adId", adId);
                });
        PromotionResponse response = promotionMapper.toResponse(active);
        log.debug("Найдено активное промо: id={}, тип={}, до {}", response.getId(), response.getType(), response.getEndDate());
        return response;
    }

    @Transactional
    public void deactivatePromotion(Long adId, UserDetailsImpl userDetails) {
        log.info("Деактивация промо для adId={} пользователем id={}", adId, userDetails.getId());

        Promotion active = promotionRepository.findActiveByAdId(adId)
                .orElseThrow(() -> {
                    log.warn("Активное промо для adId={} не найдено", adId);
                    return new ResourceNotFoundException("Promotion", "adId", adId);
                });

        boolean isOwner = active.getAd().getSeller().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            log.warn("Отказ в доступе: userId={} не владелец и не админ. Владелец объявления={}",
                    userDetails.getId(), active.getAd().getSeller().getId());
            throw new AccessDeniedException("Only the owner or admin can deactivate promotion");
        }

        active.setActive(false);
        log.info("Промо для adId={} деактивировано", adId);
    }
}