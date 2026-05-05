package classified.repository.impl;

import classified.entity.AdComment;
import classified.repository.AbstractRepository;
import classified.repository.AdCommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdCommentRepositoryImpl extends AbstractRepository<AdComment, Long> implements AdCommentRepository {
    protected AdCommentRepositoryImpl() {
        super(AdComment.class);
    }

    @Override
    public List<AdComment> findByAuthorId(Long authorId) {
        return executeWithResult("findByAuthorId",
                em -> em.createQuery("SELECT ac FROM AdComment ac WHERE ac.order.buyer.id = :authorId", AdComment.class)
                        .setParameter("authorId", authorId)
                        .getResultList(),
                "authorId=" + authorId);
    }

    @Override
    public List<AdComment> findByTargetUserId(Long targetUserId) {
        return executeWithResult("findByTargetUserId",
                em -> em.createQuery("SELECT ac FROM AdComment ac WHERE ac.order.seller.id = :targetUserId", AdComment.class)
                        .setParameter("targetUserId", targetUserId)
                        .getResultList(),
                "targetUserId=" + targetUserId);
    }

    @Override
    public List<AdComment> findByAdId(Long adId) {
        return executeWithResult("findByAdId",
                em -> em.createQuery("SELECT ac FROM AdComment ac join ac.order o WHERE ac.order.ad.id = :adId", AdComment.class)
                        .setParameter("adId", adId)
                        .getResultList(),
                "adId=" + adId);
    }

    @Override
    public Double getAverageRatingForUser(Long userId) {
        return executeWithResult("getAverageRatingForUser",
                em -> {
                    Double averageRating = em.createQuery("SELECT AVG(ac.rating) FROM AdComment ac WHERE ac.order.seller.id = :userId", Double.class)
                            .setParameter("userId", userId)
                            .getSingleResult();
                    return averageRating != null ? averageRating : 0.0;
                },
                "userId=" + userId);
    }

    @Override
    public Double getAverageRatingForAd(Long adId) {
        return executeWithResult("getAverageRatingForAd",
                em -> {
                    Double averageRating = em.createQuery("SELECT AVG(ac.rating) FROM AdComment ac join ac.order o WHERE ac.order.ad.id = :adId", Double.class)
                            .setParameter("adId", adId)
                            .getSingleResult();
                    return averageRating != null ? averageRating : 0.0;
                },
                "adId=" + adId);
    }
}
