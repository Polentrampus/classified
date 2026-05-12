package com.classified.repository;

import com.classified.entity.AdComment;

import java.util.List;

public interface AdCommentRepository extends BaseRepository<AdComment, Long> {
    List<AdComment> findByAuthorId(Long authorId);
    List<AdComment> findByTargetUserId(Long targetUserId);
    List<AdComment> findByAdId(Long adId);
    Double getAverageRatingForUser(Long userId);
    Double getAverageRatingForAd(Long adId);
}
