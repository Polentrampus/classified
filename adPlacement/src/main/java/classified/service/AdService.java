package classified.service;

import classified.dto.AdCreateRequest;
import classified.dto.AdResponse;
import classified.dto.AdSearchCriteria;
import classified.dto.AdUpdateRequest;
import classified.entity.Ad;
import classified.entity.AdStatus;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

public interface AdService {
    AdResponse createAd(AdCreateRequest request, Long sellerId);
    AdResponse updateAd(Long adId, AdUpdateRequest request, Long userId);
    void deleteAd(Long adId, Long userId);
    AdResponse getAd(Long adId);
    void changeAdStatus(Long adId, AdStatus newStatus, Long userId);
    public PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable);
}
