package com.classified.impl;

import com.classified.entity.AdImage;
import com.classified.repository.AdImageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdImageRepositoryImpl extends AbstractRepository<AdImage,Long> implements AdImageRepository {
    protected AdImageRepositoryImpl() {
        super(AdImage.class);
    }

    @Override
    public List<AdImage> findByAdId(Long adId) {
        return executeWithResult("findByAdId",
                em -> em.createQuery(
                                "SELECT ai FROM AdImage ai WHERE ai.ad.id = :adId ORDER BY ai.isMain DESC, ai.id ASC",
                                AdImage.class)
                        .setParameter("adId", adId)
                        .getResultList(),
                "adId=" + adId);
    }

    @Override
    public void deleteByAdId(Long adId) {
        execute("deleteByAdId",
                em -> em.createQuery("DELETE FROM AdImage ai WHERE ai.ad.id = :adId")
                        .setParameter("adId", adId)
                        .executeUpdate(),
                "adId=" + adId);
    }
}
