package classified.controller;

import classified.dto.message.MessageCreateRequest;
import classified.dto.message.MessageResponse;
import classified.security.UserDetailsImpl;
import classified.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "Создать сообщение", description = "Создаёт новое сообщение от имени текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сообщение создано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    ResponseEntity<MessageResponse> createMessage(@RequestBody MessageCreateRequest request,
                                                  @AuthenticationPrincipal UserDetailsImpl currentUser){
        MessageResponse response = messageService.create(request, currentUser);
        return ResponseEntity
                .created(URI
                        .create("/api/message" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Удалить сообщение", description = "Удаляет новое сообщение от имени текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объявление удалено"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @DeleteMapping("/{messageId}")
    ResponseEntity<MessageResponse> delete(@PathVariable Long messageId,
                                           @AuthenticationPrincipal UserDetailsImpl currentUser){
        messageService.delete(messageId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
