package classified.controller;

import classified.dto.chat.ChatCreateRequest;
import classified.dto.chat.ChatResponse;
import classified.security.UserDetailsImpl;
import classified.service.ChatService;
import classified.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;

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
}
