package classified.service;

import classified.dto.PromotionCreateRequest;
import classified.dto.PromotionResponse;
import classified.entity.Ad;
import classified.entity.Promotion;
import classified.entity.mappers.PromotionMapper;
import classified.exception.ErrorCode;
import classified.exception.business.BusinessException;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AdRepository;
import classified.repository.PromotionRepository;
import classified.security.UserDetailsImpl;
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

    /**
     * Создать продвижение объявления.
     * Проверяет, что пользователь — владелец объявления.
     * Проверяет, что нет активного промо.
     */
    @Transactional
    public PromotionResponse createPromotion(PromotionCreateRequest request, UserDetailsImpl userDetails) {
        // 1. Найти объявление
        Ad ad = adRepository.findById(request.getAdId())
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", request.getAdId()));

        // 2. Проверить, что пользователь — владелец
        if (!ad.getSeller().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("Only the owner can promote their ad");
        }

        // 3. Проверить, что нет активного промо
        promotionRepository.findActiveByAdId(request.getAdId()).ifPresent(p -> {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Ad already has active promotion until " + p.getEndDate());
        });

        // 4. Определить длительность
        int days = switch (request.getType()) {
            case TOP_7_DAYS -> 7;
            case TOP_30_DAYS -> 30;
            case HIGHLIGHT -> 7;
        };

        // 5. Создать промо
        Promotion promotion = new Promotion();
        promotion.setAd(ad);
        promotion.setType(request.getType());
        promotion.setStartDate(LocalDateTime.now());
        promotion.setEndDate(LocalDateTime.now().plusDays(days));
        promotion.setActive(true);

        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toResponse(saved);
    }

    /**
     * Получить активное промо объявления
     */
    public PromotionResponse getActiveByAdId(Long adId) {
        adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));

        Promotion active = promotionRepository.findActiveByAdId(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "adId", adId));
        return promotionMapper.toResponse(active);
    }

    /**
     * Деактивировать промо (владелец или админ)
     */
    @Transactional
    public void deactivatePromotion(Long adId, UserDetailsImpl userDetails) {
        Promotion active = promotionRepository.findActiveByAdId(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "adId", adId));

        boolean isOwner = active.getAd().getSeller().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Only the owner or admin can deactivate promotion");
        }

        active.setActive(false);
    }
}