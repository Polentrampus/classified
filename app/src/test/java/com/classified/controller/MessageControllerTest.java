package com.classified.controller;

import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.config.TestSecurityConfig;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.security.UserDetailsImpl;
import com.classified.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@Import(TestSecurityConfig.class)
class MessageControllerTest extends BaseControllerTest {

    @MockitoBean
    private MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsImpl createTestUser() {
        return new UserDetailsImpl(
                User.builder()
                        .id(1L)
                        .email("sender@test.com")
                        .password("encoded")
                        .role(Role.builder().name("ROLE_USER").build())
                        .build()
        );
    }

    @Test
    void shouldCreateMessage() throws Exception {
        UserDetailsImpl testUser = createTestUser();
        MessageCreateRequest request = MessageCreateRequest.builder()
                .chatId(10L)
                .content("Hello!")
                .build();

        MessageResponse response = MessageResponse.builder()
                .id(100L)
                .chatId(10L)
                .senderId(1L)
                .content("Hello!")
                .build();

        when(messageService.create(any(MessageCreateRequest.class), any(UserDetailsImpl.class))).thenReturn(response);

        mockMvc.perform(post("/api/message")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/message100"))
                .andExpect(jsonPath("$.content").value("Hello!"))
                .andExpect(jsonPath("$.senderId").value(1));
    }

    @Test
    void shouldDeleteMessage() throws Exception {
        mockMvc.perform(delete("/api/message/100")
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}