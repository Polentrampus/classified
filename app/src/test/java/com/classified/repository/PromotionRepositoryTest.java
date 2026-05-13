package com.classified.repository;

import com.classified.dto.PromotionType;
import com.classified.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User seller;
    private Ad ad1;
    private Ad ad2;
    private Promotion activePromotion;
    private Promotion expiredPromotion;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        seller = User.builder()
                .name("Seller")
                .lastName("Test")
                .email("seller@test.com")
                .phone("+79161111111")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(seller);

        City city = new City();
        city.setName("Moscow");
        entityManager.persist(city);

        Address address = Address.builder()
                .user(seller)
                .city(city)
                .build();
        entityManager.persist(address);

        ad1 = Ad.builder()
                .title("Ad 1")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad1);

        ad2 = Ad.builder()
                .title("Ad 2")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .quantity(1)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad2);

        activePromotion = new Promotion();
        activePromotion.setAd(ad1);
        activePromotion.setType(PromotionType.TOP_7_DAYS);
        activePromotion.setStartDate(LocalDateTime.now().minusDays(2));
        activePromotion.setEndDate(LocalDateTime.now().plusDays(5));
        activePromotion.setActive(true);
        entityManager.persist(activePromotion);

        expiredPromotion = new Promotion();
        expiredPromotion.setAd(ad2);
        expiredPromotion.setType(PromotionType.TOP_30_DAYS);
        expiredPromotion.setStartDate(LocalDateTime.now().minusDays(35));
        expiredPromotion.setEndDate(LocalDateTime.now().minusDays(5));
        expiredPromotion.setActive(true);
        entityManager.persist(expiredPromotion);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindActivePromotionByAdId() {
        Optional<Promotion> found = promotionRepository.findActiveByAdId(ad1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(PromotionType.TOP_7_DAYS);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoActivePromotion() {
        promotionRepository.deactivateExpiredPromotions();
        entityManager.flush();
        entityManager.clear();

        Optional<Promotion> found = promotionRepository.findActiveByAdId(ad2.getId());

        assertThat(found).isEmpty();

        Optional<Promotion> notFound = promotionRepository.findActiveByAdId(999L);
        assertThat(notFound).isEmpty();

        Optional<Promotion> activeFound = promotionRepository.findActiveByAdId(ad1.getId());
        assertThat(activeFound).isPresent();
        assertThat(activeFound.get().isActive()).isTrue();
    }

    @Test
    void shouldDeactivateExpiredPromotions() {
        promotionRepository.deactivateExpiredPromotions();
        entityManager.flush();
        entityManager.clear();

        Optional<Promotion> expired = promotionRepository.findById(expiredPromotion.getId());
        assertThat(expired).isPresent();
        assertThat(expired.get().isActive()).isFalse();

        Optional<Promotion> active = promotionRepository.findById(activePromotion.getId());
        assertThat(active).isPresent();
        assertThat(active.get().isActive()).isTrue();
    }

    @Test
    void shouldSavePromotion() {
        Promotion newPromotion = new Promotion();
        newPromotion.setAd(ad2);
        newPromotion.setType(PromotionType.HIGHLIGHT);
        newPromotion.setStartDate(LocalDateTime.now());
        newPromotion.setEndDate(LocalDateTime.now().plusDays(7));
        newPromotion.setActive(true);

        Promotion saved = promotionRepository.save(newPromotion);
        entityManager.flush();
        entityManager.clear();

        Optional<Promotion> found = promotionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(PromotionType.HIGHLIGHT);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void shouldFindPromotionById() {
        Optional<Promotion> found = promotionRepository.findById(activePromotion.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(PromotionType.TOP_7_DAYS);
    }

    @Test
    void shouldFindAllPromotions() {
        var promotions = promotionRepository.findAll();
        assertThat(promotions).hasSize(2);
    }

    @Test
    void shouldUpdatePromotion() {
        Promotion managedPromotion = entityManager.find(Promotion.class, activePromotion.getId());

        managedPromotion.setActive(false);
        promotionRepository.update(managedPromotion);
        entityManager.flush();
        entityManager.clear();

        Optional<Promotion> found = promotionRepository.findById(activePromotion.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isFalse();
    }

    @Test
    void shouldDeletePromotion() {
        Promotion managedPromotion = entityManager.find(Promotion.class, expiredPromotion.getId());
        promotionRepository.delete(managedPromotion);
        entityManager.flush();
        entityManager.clear();

        Optional<Promotion> found = promotionRepository.findById(expiredPromotion.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckPromotionExistsById() {
        assertThat(promotionRepository.existsById(activePromotion.getId())).isTrue();
        assertThat(promotionRepository.existsById(999L)).isFalse();
    }
}