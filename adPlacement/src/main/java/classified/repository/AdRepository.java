package classified.repository;

import classified.dto.AdSearchCriteria;
import classified.entity.Ad;
import classified.entity.AdStatus;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

import java.math.BigDecimal;

public interface AdRepository extends BaseRepository<Ad, Long> {
    BigDecimal getAdPrice(Long id);
    AdStatus checkStatusAd(Long id);
    void setStatusAd(Long id, AdStatus status);
    public PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable);
}
