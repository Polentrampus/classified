package com.classified.service;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.dto.AdStatus;
import com.classified.security.UserDetailsImpl;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

import java.util.List;

public interface AdService {
    AdResponse createAd(AdCreateRequest request, UserDetailsImpl userDetails);
    AdResponse updateAd(Long adId, AdUpdateRequest request, UserDetailsImpl userDetails);
    void deleteAd(Long adId, UserDetailsImpl userDetails);
    AdResponse getAd(Long adId);
    List<AdResponse> getAllAdBySellerId(Long sellerId);
    void changeAdStatus(Long adId, AdStatus newStatus, UserDetailsImpl userDetails);
    public PagedResult<AdResponse> searchAds(AdSearchCriteria criteria, PagingRequest pageable);

}
