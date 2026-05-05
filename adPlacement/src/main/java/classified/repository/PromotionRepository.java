package classified.repository;

import classified.entity.Promotion;

import java.util.Optional;

public interface PromotionRepository extends BaseRepository<Promotion, Long> {
    /**
     * Найти активное промо для объявления
     */
    Optional<Promotion> findActiveByAdId(Long adId);

    /**
     * Деактивировать все истёкшие промо
     */
    void deactivateExpiredPromotions();
}