package classified.service;

import classified.dto.AdCreateRequest;
import classified.dto.AdResponse;
import classified.dto.AdSearchCriteria;
import classified.dto.AdUpdateRequest;
import classified.entity.Ad;
import classified.entity.AdStatus;
import classified.entity.mappers.AdMapper;
import classified.exception.business.MapperException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import classified.repository.AdRepository;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class AdServiceImpl implements AdService{
    private final AdMapper adMapper;
    private final AdRepository adRepository;

    @Override
    public AdResponse createAd(AdCreateRequest request, Long sellerId) {
        Ad ad = adMapper.toEntity(request);
        if(ad == null) throw new MapperException(Ad.class, request.getDescription());
        adRepository.save(ad);
        return null;
    }

    @Override
    public AdResponse updateAd(Long adId, AdUpdateRequest request, Long userId) {
        return null;
    }

    @Override
    public void deleteAd(Long adId, Long userId) {

    }

    @Override
    public AdResponse getAd(Long adId) {
        return null;
    }

    @Override
    public void changeAdStatus(Long adId, AdStatus newStatus, Long userId) {

    }

    @Override
    public PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable) {
        return null;
    }

}
