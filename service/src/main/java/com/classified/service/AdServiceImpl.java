package com.classified.service;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.entity.Ad;
import com.classified.dto.AdStatus;
import com.classified.entity.AdType;
import com.classified.entity.Address;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdMapper;
import com.classified.repository.AdTypeRepository;
import com.classified.repository.AddressRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.classified.repository.AdRepository;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdServiceImpl implements AdService {
    private final AdMapper adMapper;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdTypeRepository adTypeRepository;
    private final AddressRepository addressRepository;

    @Transactional
    @Override
    public AdResponse createAd(AdCreateRequest request, UserDetailsImpl userDetails) {
        log.info("Создание объявления пользователем id={}", userDetails.getId());
        log.debug("Детали: title={}, price={}, adTypeId={}, addressId={}",
                request.getTitle(), request.getPrice(), request.getAdTypeId(), request.getAddressId());

        Ad ad = adMapper.toEntity(request);
        User seller = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userDetails.getId());
                    return new ResourceNotFoundException("User", "id", userDetails.getId());
                });
        ad.setSeller(seller);
        updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);
        AdResponse response = adMapper.toResponse(adRepository.save(ad));
        log.info("Объявление создано: id={}, title={}", response.getId(), response.getTitle());
        return response;
    }

    @Transactional
    @Override
    public AdResponse updateAd(Long adId, AdUpdateRequest request, UserDetailsImpl userDetails) {
        log.info("Обновление объявления id={} пользователем id={}", adId, userDetails.getId());
        log.debug("Новые данные: title={}, price={}", request.getTitle(), request.getPrice());

        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adMapper.updateEntityFromRequest(request, ad);
            updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);
            AdResponse response = adMapper.toResponse(ad);
            log.info("Объявление id={} обновлено", adId);
            return response;
        } else {
            log.warn("Отказ в доступе: userId={} не владелец объявления id={}", userDetails.getId(), adId);
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }

    @Transactional
    @Override
    public void deleteAd(Long adId, UserDetailsImpl userDetails) {
        log.info("Удаление объявления id={} пользователем id={}", adId, userDetails.getId());

        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adRepository.delete(ad);
            log.info("Объявление id={} удалено", adId);
        } else {
            log.warn("Отказ в доступе: userId={} не владелец объявления id={}", userDetails.getId(), adId);
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }

    @Override
    public AdResponse getAd(Long adId) {
        log.debug("Запрос объявления id={}", adId);
        AdResponse response = adMapper.toResponse(adRepository
                .findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                }));
        log.debug("Объявление найдено: title={}", response.getTitle());
        return response;
    }

    @Override
    public List<AdResponse> getAllAdBySellerId(Long sellerId) {
        log.debug("Запрос объявлений продавца id={}", sellerId);
        List<AdResponse> ads = adRepository.findBySellerId(sellerId).stream()
                .map(adMapper::toResponse)
                .toList();
        log.debug("Продавец id={} имеет {} объявлений", sellerId, ads.size());
        return ads;
    }

    @Transactional
    @Override
    public void changeAdStatus(Long adId, AdStatus newStatus, UserDetailsImpl userDetails) {
        log.info("Смена статуса объявления id={} на {} пользователем id={}", adId, newStatus, userDetails.getId());

        Ad ad = adRepository
                .findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ad.setStatus(newStatus);
            log.info("Статус объявления id={} изменён на {}", adId, newStatus);
        } else {
            log.warn("Отказ в доступе: userId={} не владелец объявления id={}", userDetails.getId(), adId);
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }

    @Override
    public PagedResult<AdResponse> searchAds(AdSearchCriteria criteria, PagingRequest pageable) {
        log.debug("Поиск объявлений: {}", criteria);
        PagedResult<Ad> adPagedResult = adRepository.searchAds(criteria, pageable);
        List<AdResponse> content = adPagedResult
                .getContent()
                .stream()
                .map(adMapper::toResponse)
                .toList();
        log.debug("Найдено объявлений: {} (всего {})", content.size(), adPagedResult.getTotalElements());
        return new PagedResult<>(content,
                adPagedResult.getPage(),
                adPagedResult.getSize(),
                adPagedResult.getTotalElements());
    }

    private void updateRelatedEntities(Long typeId, Long addressId, Ad ad) {
        log.debug("Обновление связей объявления: adTypeId={}, addressId={}", typeId, addressId);
        if (typeId != null) {
            AdType adType = adTypeRepository
                    .findById(typeId)
                    .orElseThrow(() -> {
                        log.warn("Тип объявления с id={} не найден", typeId);
                        return new ResourceNotFoundException("AdType", "id", typeId);
                    });
            ad.setAdType(adType);
        }
        if (addressId != null) {
            Address address = addressRepository
                    .findById(addressId)
                    .orElseThrow(() -> {
                        log.warn("Адрес с id={} не найден", addressId);
                        return new ResourceNotFoundException("Address", "id", addressId);
                    });
            ad.setAddress(address);
        }
    }
}