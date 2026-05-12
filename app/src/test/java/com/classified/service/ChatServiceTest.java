package com.classified.service;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.entity.Ad;
import com.classified.entity.Chat;
import com.classified.entity.ChatParticipant;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.ChatMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.ChatRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatService chatService;

    private User seller;
    private User buyer;
    private UserDetailsImpl buyerDetails;
    private UserDetailsImpl otherUserDetails;
    private Ad ad;
    private Chat chat;
    private ChatCreateRequest createRequest;
    private ChatResponse chatResponse;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L)
                .email("seller@test.com")
                .password("encoded")
                .role(Role.builder().name("ROLE_USER").build())
                .build();

        buyer = User.builder()
                .id(2L)
                .email("buyer@test.com")
                .password("encoded")
                .role(Role.builder().name("ROLE_USER").build())
                .build();

        buyerDetails = new UserDetailsImpl(buyer);

        otherUserDetails = new UserDetailsImpl(
                User.builder().id(999L).email("other@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );

        ad = Ad.builder()
                .id(10L)
                .title("Test Ad")
                .seller(seller)
                .build();

        createRequest = ChatCreateRequest.builder()
                .adId(10L)
                .userId(2L)
                .build();

        Set<ChatParticipant> chatParticipants = new HashSet<>();
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setUser(buyer);
        ChatParticipant chatParticipant1 = new ChatParticipant();
        chatParticipant1.setUser(seller);
        chatParticipants.add(chatParticipant);
        chatParticipants.add(chatParticipant1);

        chat = Chat.builder()
                .id(100L)
                .ad(ad)
                .participants(chatParticipants)
                .build();

        chatResponse = ChatResponse.builder()
                .id(100L)
                .adId(10L)
                .userId(2L)
                .build();
    }

    @Test
    void shouldCreateNewChatWhenNotExists() {
        Chat newChat = Chat.builder().ad(ad).build();

        when(chatRepository.findByAdIdAndBuyerId(10L, 2L)).thenReturn(Optional.empty());
        when(chatMapper.toEntity(any(ChatCreateRequest.class))).thenReturn(newChat);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRepository.save(any(Chat.class))).thenReturn(newChat);
        when(chatMapper.toResponse(any(Chat.class))).thenReturn(chatResponse);

        ChatResponse result = chatService.create(createRequest, buyerDetails);

        assertThat(result).isNotNull();
        assertThat(result.getAdId()).isEqualTo(10L);
        verify(chatRepository).save(any(Chat.class));
        verify(chatRepository).findByAdIdAndBuyerId(10L, 2L);
    }

    @Test
    void shouldReturnExistingChatWhenAlreadyExists() {
        when(chatRepository.findByAdIdAndBuyerId(10L, 2L)).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.create(createRequest, buyerDetails);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenCreatingChatForAnotherUser() {
        createRequest.setUserId(1L);

        assertThatThrownBy(() -> chatService.create(createRequest, buyerDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(chatRepository, never()).save(any());
        verify(chatRepository, never()).findByAdIdAndBuyerId(anyLong(), anyLong());
        verify(adRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void shouldDeleteChat() {
        UserDetailsImpl userDetails = new UserDetailsImpl(buyer);

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        chatService.delete(100L, userDetails);

        verify(chatRepository).delete(chat);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentChat() {
        UserDetailsImpl userDetails = new UserDetailsImpl(buyer);

        when(chatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.delete(999L, userDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingForeignChat() {
        User foreignUser = User.builder()
                .id(999L)
                .email("foreign@test.com")
                .password("encoded")
                .role(Role.builder().name("ROLE_USER").build())
                .build();
        UserDetailsImpl foreignDetails = new UserDetailsImpl(foreignUser);

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.delete(100L, foreignDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(chatRepository, never()).delete(any());
    }

    @Test
    void shouldGetChat() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.getChat(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void shouldGetAllChats() {
        UserDetailsImpl userDetails = new UserDetailsImpl(buyer);
        when(chatRepository.findByUserId(2L)).thenReturn(List.of(chat));
        when(chatMapper.toResponse(any(Chat.class))).thenReturn(chatResponse);

        List<ChatResponse> result = chatService.getAllChats(userDetails);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindChatByAdId() {
        when(chatRepository.findByAdId(10L)).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.findByAdId(10L);

        assertThat(result).isNotNull();
        assertThat(result.getAdId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowExceptionWhenChatNotFoundByAdId() {
        when(chatRepository.findByAdId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.findByAdId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindChatsByUserId() {
        when(chatRepository.findByUserId(2L)).thenReturn(List.of(chat));
        when(chatMapper.toResponse(any(Chat.class))).thenReturn(chatResponse);

        List<ChatResponse> result = chatService.findByUserId(2L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoChatsForUser() {
        when(chatRepository.findByUserId(999L)).thenReturn(List.of());

        List<ChatResponse> result = chatService.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindChatByAdIdAndBuyerId() {
        when(chatRepository.findByAdIdAndBuyerId(10L, 2L)).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.findByAdIdAndBuyerId(10L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getAdId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowExceptionWhenChatNotFoundByAdIdAndBuyerId() {
        when(chatRepository.findByAdIdAndBuyerId(10L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.findByAdIdAndBuyerId(10L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}