package com.classified.repository;

import com.classified.dto.AdStatus;
import com.classified.dto.ad.AdSearchCriteria;
import com.classified.entity.*;
import com.classified.pagination.Direction;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;
import com.classified.pagination.Sort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class AdRepositoryTest {

    @Autowired
    private AdRepository adRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User seller1;
    private User seller2;
    private City city;
    private Address address;
    private Ad ad1;
    private Ad ad2;
    private Ad ad3;
    private Ad ad4;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        seller1 = User.builder()
                .name("Seller1")
                .lastName("Test")
                .email("seller1@test.com")
                .phone("+79161111111")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(seller1);

        seller2 = User.builder()
                .name("Seller2")
                .lastName("Test")
                .email("seller2@test.com")
                .phone("+79162222222")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(seller2);

        UserRating rating1 = UserRating.builder()
                .user(seller1)
                .rating(new BigDecimal("4.50"))
                .build();
        entityManager.persist(rating1);

        UserRating rating2 = UserRating.builder()
                .user(seller2)
                .rating(new BigDecimal("2.00"))
                .build();
        entityManager.persist(rating2);

        city = new City();
        city.setName("Moscow");
        entityManager.persist(city);

        address = Address.builder()
                .user(seller1)
                .city(city)
                .build();
        entityManager.persist(address);

        ad1 = Ad.builder()
                .title("iPhone 14 Pro")
                .description("Brand new iPhone")
                .price(new BigDecimal("1000.00"))
                .quantity(5)
                .status(AdStatus.ACTIVE)
                .seller(seller1)
                .address(address)
                .build();
        entityManager.persist(ad1);

        ad2 = Ad.builder()
                .title("Samsung Galaxy S23")
                .description("Samsung smartphone")
                .price(new BigDecimal("800.00"))
                .quantity(3)
                .status(AdStatus.ACTIVE)
                .seller(seller1)
                .address(address)
                .build();
        entityManager.persist(ad2);

        ad3 = Ad.builder()
                .title("iPhone 12")
                .description("Used iPhone")
                .price(new BigDecimal("400.00"))
                .quantity(1)
                .status(AdStatus.ACTIVE)
                .seller(seller2)
                .address(address)
                .build();
        entityManager.persist(ad3);

        ad4 = Ad.builder()
                .title("MacBook Pro")
                .description("Apple laptop")
                .price(new BigDecimal("2000.00"))
                .quantity(2)
                .status(AdStatus.SOLD)
                .seller(seller1)
                .address(address)
                .build();
        entityManager.persist(ad4);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindAdsBySellerId() {
        List<Ad> ads = adRepository.findBySellerId(seller1.getId());

        assertThat(ads).hasSize(3);
        assertThat(ads).allMatch(a -> a.getSeller().getId().equals(seller1.getId()));
    }

    @Test
    void shouldReturnEmptyListForNonExistentSellerId() {
        List<Ad> ads = adRepository.findBySellerId(999L);

        assertThat(ads).isEmpty();
    }

    @Test
    void shouldSearchAdsByTitle() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle("iPhone");
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(a -> a.getTitle().contains("iPhone"));
    }

    @Test
    void shouldSearchAdsByPriceRange() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setMinPrice(new BigDecimal("500.00"));
        criteria.setMaxPrice(new BigDecimal("1500.00"));
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(a ->
                a.getPrice().compareTo(new BigDecimal("500.00")) >= 0 &&
                        a.getPrice().compareTo(new BigDecimal("1500.00")) <= 0
        );
    }

    @Test
    void shouldSearchAdsByStatus() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setStatus(AdStatus.SOLD);
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(AdStatus.SOLD);
    }

    @Test
    void shouldSearchAdsBySellerId() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setSellerId(seller2.getId());
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSeller().getId()).isEqualTo(seller2.getId());
    }

    @Test
    void shouldSearchAdsByMinSellerRating() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setMinSellerRating(4.0);
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(a -> a.getSeller().getId().equals(seller1.getId()));
    }

    @Test
    void shouldSearchAdsWithPagination() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        PagingRequest pageable = new PagingRequest(0, 2);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldSearchAdsWithSortingByPriceAsc() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        Sort sort = Sort.by("price", Direction.ASC);
        PagingRequest pageable = new PagingRequest(0, 10, sort);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(result.getContent().get(3).getPrice()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void shouldSearchAdsWithSortingByPriceDesc() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        Sort sort = Sort.by("price", Direction.DESC);
        PagingRequest pageable = new PagingRequest(0, 10, sort);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void shouldSearchAdsWithMultipleCriteria() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle("iPhone");
        criteria.setMinPrice(new BigDecimal("800.00"));
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("iPhone 14 Pro");
    }

    @Test
    void shouldReturnEmptyResultForNoMatches() {
        AdSearchCriteria criteria = new AdSearchCriteria();
        criteria.setTitle("NonExistentPhone");
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Ad> result = adRepository.searchAds(criteria, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldSaveAd() {
        Ad newAd = Ad.builder()
                .title("New Ad")
                .description("New Description")
                .price(new BigDecimal("500.00"))
                .quantity(3)
                .seller(seller2)
                .address(address)
                .build();

        Ad saved = adRepository.save(newAd);
        entityManager.flush();
        entityManager.clear();

        Optional<Ad> found = adRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("New Ad");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldFindAdById() {
        Optional<Ad> found = adRepository.findById(ad1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("iPhone 14 Pro");
    }

    @Test
    void shouldUpdateAd() {
        Ad managedAd = entityManager.find(Ad.class, ad1.getId());
        BigDecimal newPrice = new BigDecimal("900.00");

        managedAd.setPrice(newPrice);
        adRepository.update(managedAd);
        entityManager.flush();
        entityManager.clear();

        Optional<Ad> found = adRepository.findById(ad1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualByComparingTo(newPrice);
    }

    @Test
    void shouldDeleteAd() {
        Ad managedAd = entityManager.find(Ad.class, ad4.getId());
        adRepository.delete(managedAd);
        entityManager.flush();
        entityManager.clear();
        Optional<Ad> found = adRepository.findById(ad4.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckAdExistsById() {
        assertThat(adRepository.existsById(ad1.getId())).isTrue();
        assertThat(adRepository.existsById(999L)).isFalse();
    }
}