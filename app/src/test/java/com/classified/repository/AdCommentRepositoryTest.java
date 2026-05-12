package com.classified.repository;

import com.classified.dto.OrderStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class AdCommentRepositoryTest {

    @Autowired
    private AdCommentRepository adCommentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User seller;
    private User buyer;
    private Order order;
    private AdComment comment1;
    private AdComment comment2;

    @BeforeEach
    void setUp() {
        // Создаем роли
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        // Создаем продавца
        seller = User.builder()
                .name("Seller")
                .lastName("Test")
                .email("seller@test.com")
                .phone("+79165555555")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(seller);

        // Создаем покупателя
        buyer = User.builder()
                .name("Buyer")
                .lastName("Test")
                .email("buyer2@test.com")
                .phone("+79166666666")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(buyer);

        // Создаем город и адрес
        City city = new City();
        city.setName("Moscow");
        entityManager.persist(city);

        Address address = Address.builder()
                .user(seller)
                .city(city)
                .build();
        entityManager.persist(address);

        // Создаем объявления
        Ad ad1 = Ad.builder()
                .title("First Ad")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad1);

        Ad ad2 = Ad.builder()
                .title("Second Ad")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .quantity(1)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad2);

        // Создаем заказы
        order = Order.builder()
                .ad(ad1)
                .buyer(buyer)
                .seller(seller)
                .quantity(1)
                .totalPrice(new BigDecimal("100.00"))
                .status(OrderStatus.COMPLETED)
                .build();
        entityManager.persist(order);

        Order order2 = Order.builder()
                .ad(ad2)
                .buyer(buyer)
                .seller(seller)
                .quantity(1)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.COMPLETED)
                .build();
        entityManager.persist(order2);

        comment2 = AdComment.builder()
                .order(order2)
                .rating(3)
                .content("Good product")
                .build();
        entityManager.persist(comment2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindCommentsByAuthorId() {
        // when
        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);

        List<AdComment> comments = adCommentRepository.findByAuthorId(buyer.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(c -> c.getOrder().getBuyer().getId().equals(buyer.getId()));
    }

    @Test
    void shouldReturnEmptyListForNonExistentAuthor() {
        // when
        List<AdComment> comments = adCommentRepository.findByAuthorId(999L);

        // then
        assertThat(comments).isEmpty();
    }

    @Test
    void shouldFindCommentsByTargetUserId() {
        // when
        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);
        List<AdComment> comments = adCommentRepository.findByTargetUserId(seller.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(c -> c.getOrder().getSeller().getId().equals(seller.getId()));
    }

    @Test
    void shouldFindCommentsByAdId() {
        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);
        // when
        List<AdComment> comments = adCommentRepository.findByAdId(order.getAd().getId());

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getOrder().getAd().getId()).isEqualTo(order.getAd().getId());
    }

    @Test
    void shouldGetAverageRatingForUser() {
        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);
        // when
        Double averageRating = adCommentRepository.getAverageRatingForUser(seller.getId());

        // then
        assertThat(averageRating).isEqualTo(4.0); // (5 + 3) / 2
    }

    @Test
    void shouldReturnZeroAverageRatingForUserWithoutComments() {
        // when
        Double averageRating = adCommentRepository.getAverageRatingForUser(999L);

        // then
        assertThat(averageRating).isEqualTo(0.0);
    }

    @Test
    void shouldGetAverageRatingForAd() {
        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);
        // when
        Double averageRating = adCommentRepository.getAverageRatingForAd(order.getAd().getId());

        // then
        assertThat(averageRating).isEqualTo(5.0);
    }

    @Test
    void shouldReturnZeroAverageRatingForAdWithoutComments() {
        // when
        Double averageRating = adCommentRepository.getAverageRatingForAd(999L);

        // then
        assertThat(averageRating).isEqualTo(0.0);
    }

    @Test
    void shouldSaveComment() {
        // given
        AdComment newComment = AdComment.builder()
                .order(order)
                .rating(4)
                .content("Nice product")
                .build();

        // when
        AdComment saved = adCommentRepository.save(newComment);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<AdComment> found = adCommentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isEqualTo(4);
        assertThat(found.get().getContent()).isEqualTo("Nice product");
    }

    @Test
    void shouldUpdateComment() {
        // given
        String newContent = "Updated review content";

        comment1 = AdComment.builder()
                .order(order)
                .rating(5)
                .content("Excellent product!")
                .build();
        entityManager.persist(comment1);
        // when
        comment1.setContent(newContent);
        comment1.setRating(4);
        adCommentRepository.update(comment1);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<AdComment> found = adCommentRepository.findById(comment1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(newContent);
        assertThat(found.get().getRating()).isEqualTo(4);
    }
}