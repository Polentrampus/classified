package com.classified.service;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.dto.PromotionType;
import com.classified.entity.Ad;
import com.classified.entity.Promotion;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.PromotionMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.PromotionRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionService promotionService;

    private User seller;
    private UserDetailsImpl sellerDetails;
    private UserDetailsImpl otherUserDetails;
    private Ad ad;
    private PromotionCreateRequest createRequest;
    private Promotion promotion;
    private PromotionResponse promotionResponse;

    @BeforeEach
    void setUp() {
        seller = User.builder().id(1L).build();

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

        ad = Ad.builder().id(10L).seller(seller).build();

        createRequest = PromotionCreateRequest.builder()
                .adId(10L)
                .type(PromotionType.TOP_7_DAYS)
                .build();

        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setAd(ad);
        promotion.setType(PromotionType.TOP_7_DAYS);
        promotion.setStartDate(LocalDateTime.now());
        promotion.setEndDate(LocalDateTime.now().plusDays(7));
        promotion.setActive(true);

        promotionResponse = PromotionResponse.builder()
                .id(1L)
                .adId(10L)
                .type(PromotionType.TOP_7_DAYS)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .isActive(true)
                .build();
    }

    @Test
    void shouldCreatePromotion() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.empty());
        when(promotionRepository.save(any(Promotion.class))).thenReturn(promotion);
        when(promotionMapper.toResponse(any(Promotion.class))).thenReturn(promotionResponse);

        // when
        PromotionResponse result = promotionService.createPromotion(createRequest, sellerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(PromotionType.TOP_7_DAYS);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void shouldDeactivatePromotionAsAdmin() {
        // В UserDetailsImpl добавляем ROLE_ADMIN
        UserDetailsImpl adminDetails = new UserDetailsImpl(
                User.builder().id(999L).email("admin@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_ADMIN").build())
                        .build()
        );

        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.of(promotion));

        promotionService.deactivatePromotion(10L, adminDetails);

        assertThat(promotion.isActive()).isFalse();
    }

    @Test
    void shouldThrowAccessDeniedWhenNotOwnerCreatesPromotion() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        // when & then
        assertThatThrownBy(() -> promotionService.createPromotion(createRequest, otherUserDetails))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldThrowExceptionWhenActivePromotionExists() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.of(promotion));

        // when & then
        assertThatThrownBy(() -> promotionService.createPromotion(createRequest, sellerDetails))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    void shouldGetActivePromotionByAdId() {
        // given
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.of(promotion));
        when(promotionMapper.toResponse(promotion)).thenReturn(promotionResponse);

        // when
        PromotionResponse result = promotionService.getActiveByAdId(10L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenAdNotFoundForPromotion() {
        // given
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> promotionService.getActiveByAdId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeactivatePromotionAsOwner() {
        // given
        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.of(promotion));

        // when
        promotionService.deactivatePromotion(10L, sellerDetails);

        // then
        assertThat(promotion.isActive()).isFalse();
    }

    @Test
    void shouldThrowAccessDeniedWhenNotOwnerDeactivatesPromotion() {
        // given
        when(promotionRepository.findActiveByAdId(10L)).thenReturn(Optional.of(promotion));

        // when & then
        assertThatThrownBy(() -> promotionService.deactivatePromotion(10L, otherUserDetails))
                .isInstanceOf(AccessDeniedException.class);
    }
}