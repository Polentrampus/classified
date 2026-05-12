package com.classified.repository;

import com.classified.dto.ad.AdSearchCriteria;
import com.classified.entity.Ad;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

import java.util.List;

public interface AdRepository extends BaseRepository<Ad, Long> {
    PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable);
    List<Ad> findBySellerId(Long sellerId);

}
