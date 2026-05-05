package classified.repository;

import classified.dto.ad.AdSearchCriteria;
import classified.entity.Ad;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

import java.util.List;

public interface AdRepository extends BaseRepository<Ad, Long> {
    PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable);
    List<Ad> findBySellerId(Long sellerId);

}
