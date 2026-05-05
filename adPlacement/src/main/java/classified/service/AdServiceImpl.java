package classified.service;

import classified.dto.ad.AdCreateRequest;
import classified.dto.ad.AdResponse;
import classified.dto.ad.AdSearchCriteria;
import classified.dto.ad.AdUpdateRequest;
import classified.entity.Ad;
import classified.entity.AdStatus;
import classified.entity.AdType;
import classified.entity.Address;
import classified.entity.User;
import classified.entity.mappers.AdMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AdTypeRepository;
import classified.repository.AddressRepository;
import classified.repository.UserRepository;
import classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import classified.repository.AdRepository;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {
    private final AdMapper adMapper;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdTypeRepository adTypeRepository;
    private final AddressRepository addressRepository;

    @Transactional
    @Override
    public AdResponse createAd(AdCreateRequest request, UserDetailsImpl userDetails) {
        // 1) Маппим базовые поля
        if (request.getSellerId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Ad ad = adMapper.toEntity(request);
            // 2) Загружаем и устанавливаем связанные сущности
            User seller = userRepository
                    .findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
            ad.setSeller(seller);
            updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);
            return adMapper.toResponse(adRepository.save(ad));
        } else
            throw new AccessDeniedException("You can only edit your own ads");

    }

    @Transactional
    @Override
    public AdResponse updateAd(Long adId, AdUpdateRequest request, UserDetailsImpl userDetails) {
        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adMapper.updateEntityFromRequest(request, ad);
            updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);
            return adMapper.toResponse(ad);
        } else
            throw new AccessDeniedException("You can only edit your own ads");
    }

    @Transactional
    @Override
    public void deleteAd(Long adId, UserDetailsImpl userDetails) {
        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adRepository.delete(ad);

        } else
            throw new AccessDeniedException("You can only edit your own ads");
    }

    @Override
    public AdResponse getAd(Long adId) {
        return adMapper.toResponse(adRepository
                .findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId)));
    }

    @Override
    public List<AdResponse> getAllAdBySellerId(Long sellerId) {
        return adRepository.findBySellerId(sellerId).stream()
                .map(adMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void changeAdStatus(Long adId, AdStatus newStatus, UserDetailsImpl userDetails) {
        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ad.setStatus(newStatus);

        } else
            throw new AccessDeniedException("You can only edit your own ads");
    }

    @Override
    public PagedResult<AdResponse> searchAds(AdSearchCriteria criteria, PagingRequest pageable) {
        PagedResult<Ad> adPagedResult = adRepository.searchAds(criteria, pageable);
        List<AdResponse> content = adPagedResult
                .getContent()
                .stream()
                .map(adMapper::toResponse)
                .toList();

        return new PagedResult<>(content,
                adPagedResult.getPage(),
                adPagedResult.getSize(),
                adPagedResult.getTotalElements());
    }

    private void updateRelatedEntities(Long typeId, Long addressId, Ad ad) {
        if (typeId != null) {
            AdType adType = adTypeRepository
                    .findById(typeId)
                    .orElseThrow(() -> new ResourceNotFoundException("AdType", "id", typeId));
            ad.setAdType(adType);
        }
        if (addressId != null) {
            Address address = addressRepository
                    .findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
            ad.setAddress(address);
        }
    }
}
