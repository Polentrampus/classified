package com.classified.repository;

import com.classified.entity.AdImage;

import java.util.List;

public interface AdImageRepository extends BaseRepository<AdImage, Long> {
    List<AdImage> findByAdId(Long adId);
    void deleteByAdId(Long adId);
}
