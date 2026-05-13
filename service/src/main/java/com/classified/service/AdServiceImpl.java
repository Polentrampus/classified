package com.classified.service;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.dto.image.AdImageRequest;
import com.classified.entity.Ad;
import com.classified.dto.AdStatus;
import com.classified.entity.AdImage;
import com.classified.entity.AdType;
import com.classified.entity.Address;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdMapper;
import com.classified.repository.AdImageRepository;
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

import java.util.ArrayList;
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
    private final AdImageRepository adImageRepository;

    @Transactional
    @Override
    public AdResponse createAd(AdCreateRequest request, UserDetailsImpl userDetails) {
        log.info("Создание объявления пользователем id={}", userDetails.getId());

        Ad ad = adMapper.toEntity(request);
        User seller = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userDetails.getId());
                    return new ResourceNotFoundException("User", "id", userDetails.getId());
                });
        ad.setSeller(seller);
        updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);

        // Сохраняем объявление
        Ad savedAd = adRepository.save(ad);

        // Обрабатываем изображения
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<AdImage> images = new ArrayList<>();
            for (AdImageRequest imageRequest : request.getImages()) {
                AdImage image = new AdImage();
                image.setAd(savedAd);
                image.setUrl(imageRequest.getUrl());
                image.setIsMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false);
                images.add(image);
                adImageRepository.save(image);
            }
            savedAd.setImages(images);
            log.info("Добавлено {} изображений для объявления id={}", images.size(), savedAd.getId());
        }

        AdResponse response = adMapper.toResponse(savedAd);
        log.info("Объявление создано: id={}, title={}", response.getId(), response.getTitle());
        return response;
    }

    @Transactional
    @Override
    public AdResponse updateAd(Long adId, AdUpdateRequest request, UserDetailsImpl userDetails) {
        log.info("Обновление объявления id={} пользователем id={}", adId, userDetails.getId());

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });

        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adMapper.updateEntityFromRequest(request, ad);
            updateRelatedEntities(request.getAdTypeId(), request.getAddressId(), ad);

            if (request.getImages() != null) {
                adImageRepository.deleteByAdId(adId);
                if (!request.getImages().isEmpty()) {
                    List<AdImage> images = new ArrayList<>();
                    for (AdImageRequest imageRequest : request.getImages()) {
                        AdImage image = new AdImage();
                        image.setAd(ad);
                        image.setUrl(imageRequest.getUrl());
                        image.setIsMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false);
                        images.add(image);
                        adImageRepository.save(image);
                    }
                    ad.setImages(images);
                }
            }

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

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> {
                    log.warn("Объявление с id={} не найдено", adId);
                    return new ResourceNotFoundException("Ad", "id", adId);
                });

        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            adRepository.delete(ad);
            log.info("Объявление id={} удалено вместе с изображениями", adId);
        } else {
            log.warn("Отказ в доступе: userId={} не владелец объявления id={}", userDetails.getId(), adId);
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }

    @Override
    public AdResponse getAd(Long adId) {
        log.debug("Запрос объявления id={}", adId);
        AdResponse response = adMapper.toResponse(adRepository.findById(adId)
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
        return adRepository.findBySellerId(sellerId).stream()
                .map(adMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void changeAdStatus(Long adId, AdStatus newStatus, UserDetailsImpl userDetails) {
        log.info("Смена статуса объявления id={} на {} пользователем id={}", adId, newStatus, userDetails.getId());
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
        if (ad.getSeller().getId().equals(userDetails.getId()) || userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ad.setStatus(newStatus);
        } else {
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }

    @Override
    public PagedResult<AdResponse> searchAds(AdSearchCriteria criteria, PagingRequest pageable) {
        PagedResult<Ad> adPagedResult = adRepository.searchAds(criteria, pageable);
        List<AdResponse> content = adPagedResult.getContent().stream()
                .map(adMapper::toResponse)
                .toList();
        return new PagedResult<>(content,
                adPagedResult.getPage(),
                adPagedResult.getSize(),
                adPagedResult.getTotalElements());
    }

    private void updateRelatedEntities(Long typeId, Long addressId, Ad ad) {
        if (typeId != null) {
            AdType adType = adTypeRepository.findById(typeId)
                    .orElseThrow(() -> new ResourceNotFoundException("AdType", "id", typeId));
            ad.setAdType(adType);
        }
        if (addressId != null) {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
            ad.setAddress(address);
        }
    }
}