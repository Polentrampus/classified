package classified.repository.impl;

import classified.entity.Chat;
import classified.entity.ChatParticipant;
import classified.repository.AbstractRepository;
import classified.repository.ChatRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatRepositoryImpl extends AbstractRepository<Chat, Long> implements ChatRepository {

    protected ChatRepositoryImpl() {
        super(Chat.class);
    }

    @Override
    /// Найти по id объявления
    public Optional<Chat> findByAdId(Long adId) {
        return executeWithResult("findByAdId",
                em -> {
                    try {
                        return Optional.of(
                                em.createQuery("SELECT c FROM Chat c WHERE c.ad.id = :adId", Chat.class)
                                        .setParameter("adId", adId)
                                        .getSingleResult()
                        );
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                },
                "adId=" + adId);
    }

    @Override
    /// Найти все чаты по id пользователя
    public List<Chat> findByUserId(Long userId) {
        return executeWithResult("findByUserId",
                em -> em.createQuery("SELECT c FROM Chat c JOIN c.participants p WHERE p.user.id = :userId", Chat.class)
                        .setParameter("userId", userId)
                        .getResultList(),
                "userId=" + userId);
    }

    /// Чат по id объявления и id покупателя
    @Override
    public Optional<Chat> findByAdIdAndBuyerId(Long adId, Long buyerId) {
        return executeWithResult("findByAdIdAndBuyerId",
                em -> {
                    try {
                        return Optional.of(
                                em.createQuery("SELECT c FROM Chat c JOIN c.participants p WHERE c.ad.id = :adId AND p.user.id = :buyerId", Chat.class)
                                        .setParameter("adId", adId)
                                        .setParameter("buyerId", buyerId)
                                        .getSingleResult()
                        );
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                },
                "adId=" + adId + ", buyerId=" + buyerId);
    }
}
