package com.classified.controller;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.security.UserDetailsImpl;
import com.classified.service.ChatService;
import com.classified.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Переписка между покупателем и продавцом")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Создать чат", description = "Создаёт новый чат от имени текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сообщение создано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    ResponseEntity<ChatResponse> createChat(@RequestBody ChatCreateRequest request,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails){
        ChatResponse response = chatService.create(request, userDetails);
        return ResponseEntity
                .created(URI.create("/api/chat" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Получить чат по id")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    ResponseEntity<ChatResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(chatService.getChat(id));
    }

    @Operation(summary = "Получить чаты по id пользователя")
    @GetMapping("all/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    ResponseEntity<List<ChatResponse>> getChatsById(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(chatService.getAllChats(userDetails));
    }

    @Operation(summary = "Удалить чат")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Чат удалён"),
            @ApiResponse(responseCode = "404", description = "Чат не найден")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.delete(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
