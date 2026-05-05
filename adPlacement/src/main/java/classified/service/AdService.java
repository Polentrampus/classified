package classified.service;

import classified.dto.ad.AdCreateRequest;
import classified.dto.ad.AdResponse;
import classified.dto.ad.AdSearchCriteria;
import classified.dto.ad.AdUpdateRequest;
import classified.entity.AdStatus;
import classified.security.UserDetailsImpl;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

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
