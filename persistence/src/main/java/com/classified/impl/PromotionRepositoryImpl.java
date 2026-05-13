package com.classified.impl;

import com.classified.entity.Promotion;
import org.springframework.stereotype.Repository;
import com.classified.repository.PromotionRepository;

import java.util.Optional;

@Repository
public class PromotionRepositoryImpl extends AbstractRepository<Promotion, Long> implements PromotionRepository {

    protected PromotionRepositoryImpl() {
        super(Promotion.class);
    }

    @Override
    public Optional<Promotion> findActiveByAdId(Long adId) {
        return executeWithResult("findActiveByAdId",
                em -> {
                    try {
                        return Optional.of(
                                em.createQuery(
                                                "SELECT p FROM Promotion p WHERE p.ad.id = :adId " +
                                                        "AND p.active = true AND p.endDate > CURRENT_TIMESTAMP " +
                                                        "ORDER BY p.endDate DESC", Promotion.class)
                                        .setParameter("adId", adId)
                                        .setMaxResults(1)
                                        .getSingleResult()
                        );
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                }, "adId=" + adId);
    }

    @Override
    public void deactivateExpiredPromotions() {
        execute("deactivateExpired", em -> {
            em.createQuery("UPDATE Promotion p SET p.active = false " +
                            "WHERE p.active = true AND p.endDate < CURRENT_TIMESTAMP")
                    .executeUpdate();
        }, "deactivateExpiredPromotions");
    }
}