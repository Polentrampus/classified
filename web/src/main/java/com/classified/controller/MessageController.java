package com.classified.controller;

import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.dto.message.MessageUpdateRequest;
import com.classified.security.UserDetailsImpl;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
@Tag(name = "Message", description = "Сообщения в чате")
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "Создать сообщение", description = "Создаёт новое сообщение от имени текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сообщение создано"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
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
            @ApiResponse(responseCode = "204", description = "Сообщение удалено"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    ResponseEntity<MessageResponse> delete(@PathVariable Long messageId,
                                           @AuthenticationPrincipal UserDetailsImpl currentUser){
        messageService.delete(messageId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменить сообщение", description = "Переписать сообщение, которое принадлежит пользователю")
    @GetMapping("update/{messageId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    ResponseEntity<MessageResponse> update(@RequestBody MessageUpdateRequest request,
                                           @PathVariable Long messageId,
                                           @AuthenticationPrincipal UserDetailsImpl currentUser){
        return ResponseEntity.ok(messageService.update(request, messageId, currentUser));
    }

}
