package com.classified.service;

import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.dto.message.MessageUpdateRequest;
import com.classified.entity.Chat;
import com.classified.entity.Message;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.MessageMapper;
import com.classified.repository.ChatRepository;
import com.classified.repository.MessageRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @Transactional
    public MessageResponse create(MessageCreateRequest request, UserDetailsImpl userDetails) {
        log.info("Создание сообщения: chatId={}, senderId={}", request.getChatId(), request.getSenderId());
        log.debug("Контент сообщения: {}", request.getContent());

        if(!request.getSenderId().equals(userDetails.getId())) {
            log.warn("Отказ в доступе: senderId из запроса {} не совпадает с текущим пользователем {}",
                    request.getSenderId(), userDetails.getId());
            throw new AccessDeniedException("You can only edit your own ads");
        }
        Message message = messageMapper.toEntity(request);
        updateRelatedEntities(request.getChatId(), request.getSenderId(), message);
        MessageResponse response = messageMapper.toResponse(messageRepository.save(message));
        log.info("Сообщение создано: id={}, chatId={}", response.getId(), response.getChatId());
        return response;
    }

    @Transactional
    public void delete(Long messageId, UserDetailsImpl userDetails) {
        log.info("Удаление сообщения id={} пользователем id={}", messageId, userDetails.getId());

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> {
                    log.warn("Сообщение с id={} не найдено", messageId);
                    return new ResourceNotFoundException("Message", "id", messageId);
                });
        if(!message.getSender().getId().equals(userDetails.getId())) {
            log.warn("Отказ в доступе: сообщение id={} принадлежит senderId={}, а не userId={}",
                    messageId, message.getSender().getId(), userDetails.getId());
            throw new AccessDeniedException("You can only edit your own ads");
        }
        messageRepository.delete(message);
        log.info("Сообщение id={} удалено", messageId);
    }

    @Transactional
    public MessageResponse update(MessageUpdateRequest request, Long messageId, UserDetailsImpl userDetails) {
        log.info("Обновление сообщения id={} пользователем id={}", messageId, userDetails.getId());
        log.debug("Новый контент: {}", request.getContent());

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> {
                    log.warn("Сообщение с id={} не найдено", messageId);
                    return new ResourceNotFoundException("Message", "id", messageId);
                });
        if(!message.getSender().getId().equals(userDetails.getId())) {
            log.warn("Отказ в доступе: сообщение id={} принадлежит senderId={}, а не userId={}",
                    messageId, message.getSender().getId(), userDetails.getId());
            throw new AccessDeniedException("You can only edit your own ads");
        }
        messageMapper.updateEntityFromRequest(request, message);
        MessageResponse response = messageMapper.toResponse(message);
        log.info("Сообщение id={} обновлено", messageId);
        return response;
    }

    public MessageResponse getMessage(Long messageId) {
        log.debug("Запрос сообщения id={}", messageId);
        MessageResponse response = messageMapper.toResponse(messageRepository
                .findById(messageId)
                .orElseThrow(() -> {
                    log.warn("Сообщение с id={} не найдено", messageId);
                    return new ResourceNotFoundException("Message", "id", messageId);
                }));
        log.debug("Сообщение найдено: chatId={}, senderId={}", response.getChatId(), response.getSenderId());
        return response;
    }

    public List<MessageResponse> getAllMessage() {
        log.debug("Запрос всех сообщений");
        List<MessageResponse> messages = messageRepository.findAll().stream()
                .map(messageMapper::toResponse).toList();
        log.debug("Найдено {} сообщений", messages.size());
        return messages;
    }

    public PagedResult<MessageResponse> findByChatId(Long chatId, PagingRequest pageable){
        log.debug("Поиск сообщений чата id={}, страница={}, размер={}", chatId, pageable.getPage(), pageable.getSize());
        PagedResult<Message> messagePagedResult = messageRepository.findByChatId(chatId, pageable);

        List<MessageResponse> content = messagePagedResult
                .getContent()
                .stream()
                .map(messageMapper::toResponse)
                .toList();

        log.debug("Найдено {} сообщений (всего {})", content.size(), messagePagedResult.getTotalElements());
        return new PagedResult<>(content,
                messagePagedResult.getPage(),
                messagePagedResult.getSize(),
                messagePagedResult.getTotalElements());
    }

    public PagedResult<MessageResponse> findBySenderId(Long senderId, PagingRequest pageable){
        log.debug("Поиск сообщений отправителя id={}, страница={}, размер={}", senderId, pageable.getPage(), pageable.getSize());
        PagedResult<Message> messagePagedResult = messageRepository.findBySenderId(senderId, pageable);

        List<MessageResponse> content = messagePagedResult
                .getContent()
                .stream()
                .map(messageMapper::toResponse)
                .toList();

        log.debug("Найдено {} сообщений (всего {})", content.size(), messagePagedResult.getTotalElements());
        return new PagedResult<>(content,
                messagePagedResult.getPage(),
                messagePagedResult.getSize(),
                messagePagedResult.getTotalElements());
    }

    private void updateRelatedEntities(Long chatId, Long senderId, Message message) {
        log.debug("Обновление связей сообщения: chatId={}, senderId={}", chatId, senderId);
        if (chatId != null) {
            Chat chat = chatRepository
                    .findById(chatId)
                    .orElseThrow(() -> {
                        log.warn("Чат с id={} не найден", chatId);
                        return new ResourceNotFoundException("Chat", "id", chatId);
                    });
            message.setChat(chat);
        }
        if (senderId != null) {
            User sender = userRepository
                    .findById(senderId)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с id={} не найден", senderId);
                        return new ResourceNotFoundException("User", "id", senderId);
                    });
            message.setSender(sender);
        }
    }
}