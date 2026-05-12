package com.classified.service;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.dto.AdStatus;
import com.classified.entity.Ad;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AdMapper;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;
import com.classified.repository.AdRepository;
import com.classified.repository.AdTypeRepository;
import com.classified.repository.AddressRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdMapper adMapper;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdTypeRepository adTypeRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AdServiceImpl adService;

    private User seller;
    private UserDetailsImpl sellerDetails;
    private User otherUser;
    private UserDetailsImpl otherUserDetails;
    private Ad ad;
    private AdCreateRequest createRequest;
    private AdUpdateRequest updateRequest;
    private AdResponse adResponse;

    @BeforeEach
    void setUp() {
        seller = User.builder().id(1L).email("seller@test.com").build();
        otherUser = User.builder().id(2L).email("other@test.com").build();

        sellerDetails = new UserDetailsImpl(
                User.builder().id(1L).email("seller@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        otherUserDetails = new UserDetailsImpl(
                User.builder().id(2L).email("other@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        createRequest = AdCreateRequest.builder()
                .title("Test Ad")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .build();

        updateRequest = AdUpdateRequest.builder()
                .title("Updated Ad")
                .description("Updated Description")
                .price(new BigDecimal("200.00"))
                .quantity(10)
                .build();

        ad = Ad.builder()
                .id(10L)
                .title("Test Ad")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .seller(seller)
                .status(AdStatus.ACTIVE)
                .build();

        adResponse = AdResponse.builder()
                .id(10L)
                .title("Test Ad")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .sellerId(1L)
                .build();
    }

    @Test
    void shouldCreateAd() {
        // given
        when(adMapper.toEntity(any(AdCreateRequest.class))).thenReturn(ad);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(adRepository.save(any(Ad.class))).thenReturn(ad);
        when(adMapper.toResponse(any(Ad.class))).thenReturn(adResponse);

        // when
        AdResponse result = adService.createAd(createRequest, sellerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Ad");
        verify(adRepository).save(any(Ad.class));
    }

    @Test
    void shouldThrowExceptionWhenSellerNotFound() {
        // given
        when(adMapper.toEntity(any(AdCreateRequest.class))).thenReturn(ad);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adService.createAd(createRequest, sellerDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateOwnAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        doNothing().when(adMapper).updateEntityFromRequest(any(), any());
        when(adMapper.toResponse(any(Ad.class))).thenReturn(adResponse);

        // when
        AdResponse result = adService.updateAd(10L, updateRequest, sellerDetails);

        // then
        assertThat(result).isNotNull();
        verify(adMapper).updateEntityFromRequest(updateRequest, ad);
    }

    @Test
    void shouldSearchAds() {
        // given
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle("Test");
        PagingRequest pageable = new PagingRequest(0, 10);
        PagedResult<Ad> pagedResult = new PagedResult<>(List.of(ad), 0, 10, 1L);

        when(adRepository.searchAds(any(AdSearchCriteria.class), any(PagingRequest.class)))
                .thenReturn(pagedResult);
        when(adMapper.toResponse(any(Ad.class))).thenReturn(adResponse);

        // when
        PagedResult<AdResponse> result = adService.searchAds(criteria, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void shouldGetAllAdsBySellerId() {
        // given
        when(adRepository.findBySellerId(1L)).thenReturn(List.of(ad));
        when(adMapper.toResponse(any(Ad.class))).thenReturn(adResponse);

        // when
        List<AdResponse> result = adService.getAllAdBySellerId(1L);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowAccessDeniedWhenUpdatingForeignAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when & then (otherUserDetails не владелец)
        assertThatThrownBy(() -> adService.updateAd(10L, updateRequest, otherUserDetails))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldDeleteOwnAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when
        adService.deleteAd(10L, sellerDetails);

        // then
        verify(adRepository).delete(ad);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingForeignAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when & then
        assertThatThrownBy(() -> adService.deleteAd(10L, otherUserDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(adRepository, never()).delete(any());
    }

    @Test
    void shouldGetAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adMapper.toResponse(ad)).thenReturn(adResponse);

        // when
        AdResponse result = adService.getAd(10L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowExceptionWhenAdNotFound() {
        // given
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adService.getAd(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldChangeAdStatusAsOwner() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when
        adService.changeAdStatus(10L, AdStatus.SOLD, sellerDetails);

        // then
        assertThat(ad.getStatus()).isEqualTo(AdStatus.SOLD);
    }

    @Test
    void shouldThrowAccessDeniedWhenChangingStatusOfForeignAd() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when & then
        assertThatThrownBy(() -> adService.changeAdStatus(10L, AdStatus.SOLD, otherUserDetails))
                .isInstanceOf(AccessDeniedException.class);
    }
}