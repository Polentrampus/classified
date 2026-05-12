package com.classified.service;

import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.dto.message.MessageUpdateRequest;
import com.classified.entity.Chat;
import com.classified.entity.Message;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.MessageMapper;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;
import com.classified.repository.ChatRepository;
import com.classified.repository.MessageRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    private MessageCreateRequest createRequest;
    private Message message;
    private MessageResponse messageResponse;
    private UserDetailsImpl senderDetails;
    private Chat chat;
    private User sender;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).email("sender@test.com").build();

        chat = Chat.builder().id(10L).build();

        createRequest = MessageCreateRequest.builder()
                .chatId(10L)
                .senderId(1L)
                .content("Hello!")
                .build();

        message = Message.builder()
                .id(100L)
                .chat(chat)
                .sender(sender)
                .content("Hello!")
                .build();

        messageResponse = MessageResponse.builder()
                .id(100L)
                .chatId(10L)
                .senderId(1L)
                .content("Hello!")
                .build();

        senderDetails = new UserDetailsImpl(
                User.builder().id(1L).email("sender@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldCreateMessage() {
        // given
        when(messageMapper.toEntity(any(MessageCreateRequest.class))).thenReturn(message);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(messageResponse);

        // when
        MessageResponse result = messageService.create(createRequest, senderDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello!");
        assertThat(result.getSenderId()).isEqualTo(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void shouldThrowAccessDeniedWhenCreatingMessageForAnotherUser() {
        // given
        createRequest.setSenderId(999L); // чужой senderId

        // when & then
        assertThatThrownBy(() -> messageService.create(createRequest, senderDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOwnMessage() {
        // given
        when(messageRepository.findById(100L)).thenReturn(Optional.of(message));

        // when
        messageService.delete(100L, senderDetails);

        // then
        verify(messageRepository).delete(message);
    }

    @Test
    void shouldFindMessagesBySenderId() {
        PagingRequest pageable = new PagingRequest(0, 10);
        PagedResult<Message> pagedMessages = new PagedResult<>(List.of(message), 0, 10, 1L);

        when(messageRepository.findBySenderId(1L, pageable)).thenReturn(pagedMessages);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(messageResponse);

        PagedResult<MessageResponse> result = messageService.findBySenderId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(messageRepository).findBySenderId(1L, pageable);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingForeignMessage() {
        // given
        User foreignUser = User.builder().id(2L).build();
        Message foreignMessage = Message.builder().id(100L).sender(foreignUser).build();
        when(messageRepository.findById(100L)).thenReturn(Optional.of(foreignMessage));

        // when & then
        assertThatThrownBy(() -> messageService.delete(100L, senderDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(messageRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateOwnMessage() {
        // given
        MessageUpdateRequest updateRequest = MessageUpdateRequest.builder()
                .content("Updated content")
                .build();

        when(messageRepository.findById(100L)).thenReturn(Optional.of(message));
        doNothing().when(messageMapper).updateEntityFromRequest(any(), any());
        when(messageMapper.toResponse(any(Message.class))).thenReturn(messageResponse);

        // when
        MessageResponse result = messageService.update(updateRequest, 100L, senderDetails);

        // then
        assertThat(result).isNotNull();
        verify(messageMapper).updateEntityFromRequest(updateRequest, message);
    }

    @Test
    void shouldThrowExceptionWhenMessageNotFound() {
        // given
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.getMessage(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldFindByChatId() {
        // given
        PagingRequest pageable = new PagingRequest(0, 10);
        PagedResult<Message> pagedMessages = new PagedResult<>(
                List.of(message), 0, 10, 1L);
        when(messageRepository.findByChatId(10L, pageable)).thenReturn(pagedMessages);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(messageResponse);

        // when
        PagedResult<MessageResponse> result = messageService.findByChatId(10L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(messageRepository).findByChatId(10L, pageable);
    }
}