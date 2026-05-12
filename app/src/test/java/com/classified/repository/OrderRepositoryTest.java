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
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User seller;
    private User buyer;
    private Ad ad1;
    private Ad ad2;
    private Order order1;
    private Order order2;
    private Order order3;

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
                .email("seller3@test.com")
                .phone("+79167777777")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(seller);

        // Создаем покупателя
        buyer = User.builder()
                .name("Buyer")
                .lastName("Test")
                .email("buyer3@test.com")
                .phone("+79168888888")
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
        ad1 = Ad.builder()
                .title("Ad 1")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad1);

        ad2 = Ad.builder()
                .title("Ad 2")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .quantity(5)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad2);

        // Создаем заказы с разными статусами
        order1 = Order.builder()
                .ad(ad1)
                .buyer(buyer)
                .seller(seller)
                .quantity(2)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .build();
        entityManager.persist(order1);

        order2 = Order.builder()
                .ad(ad2)
                .buyer(buyer)
                .seller(seller)
                .quantity(1)
                .totalPrice(new BigDecimal("200.00"))
                .status(OrderStatus.COMPLETED)
                .build();
        entityManager.persist(order2);

        order3 = Order.builder()
                .ad(ad1)
                .buyer(buyer)
                .seller(seller)
                .quantity(3)
                .totalPrice(new BigDecimal("300.00"))
                .status(OrderStatus.CANCELLED)
                .build();
        entityManager.persist(order3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindOrdersByAdId() {
        // when
        List<Order> orders = orderRepository.findByAdId(ad1.getId());

        // then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getAd().getId().equals(ad1.getId()));
    }

    @Test
    void shouldReturnEmptyListForNonExistentAdId() {
        // when
        List<Order> orders = orderRepository.findByAdId(999L);

        // then
        assertThat(orders).isEmpty();
    }

    @Test
    void shouldFindOrdersByBuyerId() {
        // when
        List<Order> orders = orderRepository.findByBuyerId(buyer.getId());

        // then
        assertThat(orders).hasSize(3);
        assertThat(orders).allMatch(o -> o.getBuyer().getId().equals(buyer.getId()));
    }

    @Test
    void shouldFindOrdersBySellerId() {
        // when
        List<Order> orders = orderRepository.findBySellerId(seller.getId());

        // then
        assertThat(orders).hasSize(3);
        assertThat(orders).allMatch(o -> o.getSeller().getId().equals(seller.getId()));
    }

    @Test
    void shouldFindOrdersByStatus() {
        // when
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);
        List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);

        // then
        assertThat(pendingOrders).hasSize(1);
        assertThat(completedOrders).hasSize(1);
        assertThat(cancelledOrders).hasSize(1);
    }

    @Test
    void shouldSaveOrder() {
        // given
        Order newOrder = Order.builder()
                .ad(ad2)
                .buyer(buyer)
                .seller(seller)
                .quantity(2)
                .totalPrice(new BigDecimal("400.00"))
                .status(OrderStatus.PENDING)
                .build();

        // when
        Order saved = orderRepository.save(newOrder);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Order> found = orderRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getTotalPrice()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void shouldUpdateOrder() {
        // given
        OrderStatus newStatus = OrderStatus.SHIPPED;

        // when
        order1.setStatus(newStatus);
        orderRepository.update(order1);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Order> found = orderRepository.findById(order1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void shouldDeleteOrder() {
        // when
        orderRepository.delete(order3);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Order> found = orderRepository.findById(order3.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckOrderExistsById() {
        // when & then
        assertThat(orderRepository.existsById(order1.getId())).isTrue();
        assertThat(orderRepository.existsById(999L)).isFalse();
    }
}