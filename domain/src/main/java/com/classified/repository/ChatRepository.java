package com.classified.repository;

import com.classified.entity.Chat;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends BaseRepository<Chat, Long> {
    Optional<Chat> findByAdId(Long adId);
    List<Chat> findByUserId(Long userId);
    Optional<Chat> findByAdIdAndBuyerId(Long adId, Long buyerId);
}
