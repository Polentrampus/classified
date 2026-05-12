package com.classified.controller;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@Import(TestSecurityConfig.class)
class ChatControllerTest extends BaseControllerTest {

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private com.classified.service.MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createTestUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("buyer@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldCreateChat() throws Exception {
        ChatCreateRequest request = ChatCreateRequest.builder()
                .adId(10L)
                .userId(1L)
                .build();

        ChatResponse response = ChatResponse.builder()
                .id(100L)
                .adId(10L)
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        when(chatService.create(any(ChatCreateRequest.class), any(UserDetailsImpl.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .with(user(createTestUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/chat100"))
                .andExpect(jsonPath("$.adId").value(10));
    }
}