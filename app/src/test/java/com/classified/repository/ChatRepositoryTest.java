package com.classified.repository;

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
public class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User seller;
    private User buyer;
    private Ad ad;
    private Chat chat;

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

        buyer = User.builder()
                .name("Buyer")
                .lastName("Test")
                .email("buyer@test.com")
                .phone("+79162222222")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(buyer);

        City city = new City();
        city.setName("Moscow");
        entityManager.persist(city);

        Address address = Address.builder()
                .user(seller)
                .city(city)
                .build();
        entityManager.persist(address);

        ad = Ad.builder()
                .title("Test Ad")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .seller(seller)
                .address(address)
                .build();
        entityManager.persist(ad);

        chat = Chat.builder()
                .ad(ad)
                .build();
        chat.addParticipant(seller);
        chat.addParticipant(buyer);
        entityManager.persist(chat);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindChatByAdId() {
        Optional<Chat> found = chatRepository.findByAdId(ad.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAd().getId()).isEqualTo(ad.getId());
    }

    @Test
    void shouldReturnEmptyWhenChatNotFoundByAdId() {
        Optional<Chat> found = chatRepository.findByAdId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindChatsByUserId() {
        List<Chat> buyerChats = chatRepository.findByUserId(buyer.getId());
        List<Chat> sellerChats = chatRepository.findByUserId(seller.getId());

        assertThat(buyerChats).hasSize(1);
        assertThat(sellerChats).hasSize(1);
        assertThat(buyerChats.get(0).getId()).isEqualTo(chat.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoChatsForUser() {
        List<Chat> chats = chatRepository.findByUserId(999L);

        assertThat(chats).isEmpty();
    }

    @Test
    void shouldFindChatByAdIdAndBuyerId() {
        Optional<Chat> found = chatRepository.findByAdIdAndBuyerId(ad.getId(), buyer.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAd().getId()).isEqualTo(ad.getId());
    }

    @Test
    void shouldReturnEmptyWhenChatNotFoundByAdIdAndBuyerId() {
        Optional<Chat> found = chatRepository.findByAdIdAndBuyerId(ad.getId(), 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveChat() {
        User managedSeller = entityManager.find(User.class, seller.getId());
        User managedBuyer = entityManager.find(User.class, buyer.getId());
        Ad managedAd = entityManager.find(Ad.class, ad.getId());

        // Сначала сохраняем чат без участников
        Chat newChat = Chat.builder()
                .ad(managedAd)
                .build();
        Chat savedChat = chatRepository.save(newChat);
        entityManager.flush(); // чтобы получить id чата

        savedChat.addParticipant(managedSeller);
        savedChat.addParticipant(managedBuyer);
        chatRepository.update(savedChat);
        entityManager.flush();
        entityManager.clear();

        Optional<Chat> found = chatRepository.findById(savedChat.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getParticipants()).hasSize(2);
    }

    @Test
    void shouldDeleteChat() {
        chatRepository.delete(chat);
        entityManager.flush();
        entityManager.clear();

        Optional<Chat> found = chatRepository.findById(chat.getId());
        assertThat(found).isEmpty();
    }
}