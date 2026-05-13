package com.classified.repository;

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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User sender;
    private User receiver;
    private Chat chat;
    private Message message1;
    private Message message2;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        sender = User.builder()
                .name("Sender")
                .lastName("Test")
                .email("sender@test.com")
                .phone("+79163333333")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(sender);

        receiver = User.builder()
                .name("Receiver")
                .lastName("Test")
                .email("receiver@test.com")
                .phone("+79164444444")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(receiver);

        City city = new City();
        city.setName("Moscow");
        entityManager.persist(city);

        Address address = Address.builder()
                .user(sender)
                .city(city)
                .build();
        entityManager.persist(address);

        Ad ad = Ad.builder()
                .title("Test Ad")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .seller(sender)
                .address(address)
                .build();
        entityManager.persist(ad);

        chat = Chat.builder()
                .ad(ad)
                .build();
        chat.addParticipant(sender);
        chat.addParticipant(receiver);
        entityManager.persist(chat);

        message1 = Message.builder()
                .chat(chat)
                .sender(sender)
                .content("First message")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        entityManager.persist(message1);

        message2 = Message.builder()
                .chat(chat)
                .sender(receiver)
                .content("Second message")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persist(message2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindMessagesByChatIdWithPagination() {
        PagingRequest pageable = new PagingRequest(0, 10, Sort.by("createdAt", Direction.DESC));

        PagedResult<Message> result = messageRepository.findByChatId(chat.getId(), pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        // Проверяем сортировку по убыванию
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Second message");
    }

    @Test
    void shouldFindMessagesByChatIdWithPaginationAndOffset() {
        PagingRequest pageable = new PagingRequest(0, 1);

        PagedResult<Message> result = messageRepository.findByChatId(chat.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyPageForNonExistentChat() {
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Message> result = messageRepository.findByChatId(999L, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldFindMessagesBySenderId() {
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Message> result = messageRepository.findBySenderId(sender.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSender().getId()).isEqualTo(sender.getId());
        assertThat(result.getContent().get(0).getContent()).isEqualTo("First message");
    }

    @Test
    void shouldReturnEmptyPageForNonExistentSender() {
        PagingRequest pageable = new PagingRequest(0, 10);

        PagedResult<Message> result = messageRepository.findBySenderId(999L, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldSaveMessage() {
        Chat managedChat = entityManager.find(Chat.class, chat.getId());
        User managedSender = entityManager.find(User.class, sender.getId());

        Message newMessage = Message.builder()
                .chat(managedChat)
                .sender(managedSender)
                .content("New message")
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(newMessage);
        entityManager.flush();
        entityManager.clear();

        Optional<Message> found = messageRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("New message");
        assertThat(found.get().getSender().getId()).isEqualTo(sender.getId());
    }

    @Test
    void shouldUpdateMessage() {
        String newContent = "Updated content";

        message1.setContent(newContent);
        messageRepository.update(message1);
        entityManager.flush();
        entityManager.clear();

        Optional<Message> found = messageRepository.findById(message1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(newContent);
    }

    @Test
    void shouldDeleteMessage() {
        messageRepository.delete(message1);
        entityManager.flush();
        entityManager.clear();

        Optional<Message> found = messageRepository.findById(message1.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckMessageExistsById() {
        assertThat(messageRepository.existsById(message1.getId())).isTrue();
        assertThat(messageRepository.existsById(999L)).isFalse();
    }

    @Test
    void shouldPaginateCorrectlyWithMultiplePages() {
        for (int i = 3; i <= 12; i++) {
            Message msg = Message.builder()
                    .chat(chat)
                    .sender(sender)
                    .content("Message " + i)
                    .createdAt(LocalDateTime.now().plusHours(i))
                    .build();
            entityManager.persist(msg);
        }
        entityManager.flush();

        PagingRequest firstPage = new PagingRequest(0, 5);
        PagingRequest secondPage = new PagingRequest(1, 5);
        PagedResult<Message> firstResult = messageRepository.findByChatId(chat.getId(), firstPage);
        PagedResult<Message> secondResult = messageRepository.findByChatId(chat.getId(), secondPage);

        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(12);
        assertThat(firstResult.getTotalPages()).isEqualTo(3);
    }
}