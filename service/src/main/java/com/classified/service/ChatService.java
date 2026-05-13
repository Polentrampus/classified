package com.classified.service;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.entity.Ad;
import com.classified.entity.Chat;
import com.classified.entity.ChatParticipant;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.ChatMapper;
import com.classified.repository.AdRepository;
import com.classified.repository.ChatRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public ChatResponse create(ChatCreateRequest request, UserDetailsImpl userDetails) {
        log.info("Создание чата: adId={}, userId={}", request.getAdId(), request.getUserId());

        Optional<Chat> existing = chatRepository.findByAdIdAndBuyerId(request.getAdId(), request.getUserId());
        if (existing.isPresent()) {
            log.info("Чат уже существует: id={}", existing.get().getId());
            return chatMapper.toResponse(existing.get());
        }

        Chat chat = chatMapper.toEntity(request);
        chatRepository.save(chat);
        Long adId = request.getAdId();
        Long userId = request.getUserId();
        log.debug("Обновление связей чата: adId={}, userId={}", adId, userId);
        if (adId != null) {
            Ad ad = adRepository
                    .findById(adId)
                    .orElseThrow(() -> {
                        log.warn("Объявление с id={} не найдено", adId);
                        return new ResourceNotFoundException("Ad", "id", adId);
                    });
            chat.setAd(ad);
            chat.addParticipant(ad.getSeller());
            log.debug("Добавлен продавец id={} как участник чата", ad.getSeller().getId());
        }
        if (userId != null) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с id={} не найден", userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            chat.addParticipant(user);
            log.debug("Добавлен пользователь id={} как участник чата", userId);
        }
        updateRelatedEntities(request.getAdId(), request.getUserId(), chat);
        ChatResponse response = chatMapper.toResponse(chat);
        log.info("Чат создан: id={}, adId={}", response.getId(), response.getAdId());
        return response;
    }

    @Transactional
    public void delete(Long chatId, UserDetailsImpl userDetails) {
        log.info("Удаление чата id={} пользователем id={}", chatId, userDetails.getId());

        Chat chat = chatRepository
                .findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Чат с id={} не найден", chatId);
                    return new ResourceNotFoundException("Chat", "id", chatId);
                });

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userDetails.getId()));

        if (!isParticipant) {
            log.warn("Отказ в доступе: пользователь id={} не является участником чата id={}",
                    userDetails.getId(), chatId);
            throw new AccessDeniedException("You can only delete your own chats");
        }

        chatRepository.delete(chat);
        log.info("Чат id={} успешно удалён", chatId);
    }

    public ChatResponse getChat(Long chatId) {
        log.debug("Запрос чата по id={}", chatId);
        ChatResponse response = chatMapper.toResponse(chatRepository
                .findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Чат с id={} не найден", chatId);
                    return new ResourceNotFoundException("Chat", "id", chatId);
                }));
        log.debug("Чат найден: {}", response);
        return response;
    }

    public List<ChatResponse> getAllChats(UserDetailsImpl userDetails) {
        log.debug("Запрос всех чатов для пользователя id={}", userDetails.getId());
        List<ChatResponse> chats = chatRepository.findByUserId(userDetails.getId()).stream()
                .map(chatMapper::toResponse)
                .toList();
        log.debug("Найдено {} чатов для пользователя id={}", chats.size(), userDetails.getId());
        return chats;
    }

    public ChatResponse findByAdId(Long adId) {
        log.debug("Поиск чата по adId={}", adId);
        ChatResponse response = chatMapper
                .toResponse(chatRepository
                        .findByAdId(adId)
                        .orElseThrow(() -> {
                            log.warn("Чат для объявления adId={} не найден", adId);
                            return new ResourceNotFoundException("Chat", "adId", adId);
                        }));
        log.debug("Чат для adId={} найден: id={}", adId, response.getId());
        return response;
    }

    public List<ChatResponse> findByUserId(Long userId) {
        log.debug("Поиск чатов для userId={}", userId);
        List<Chat> chats = chatRepository.findByUserId(userId);
        log.debug("Найдено {} чатов для userId={}", chats.size(), userId);
        return chats.stream().map(chatMapper::toResponse).toList();
    }

    public ChatResponse findByAdIdAndBuyerId(Long adId, Long buyerId) {
        log.debug("Поиск чата: adId={}, buyerId={}", adId, buyerId);
        ChatResponse response = chatMapper
                .toResponse(chatRepository
                        .findByAdIdAndBuyerId(adId, buyerId)
                        .orElseThrow(() -> {
                            log.warn("Чат для adId={} и buyerId={} не найден", adId, buyerId);
                            return new ResourceNotFoundException("Chat", "adId and buyerId", adId, buyerId);
                        }));
        log.debug("Чат найден: id={}", response.getId());
        return response;
    }


    private void updateRelatedEntities(Long adId, Long userId, Chat chat) {
        log.debug("Обновление связей чата: adId={}, userId={}", adId, userId);
        if (adId != null) {
            Ad ad = adRepository
                    .findById(adId)
                    .orElseThrow(() -> {
                        log.warn("Объявление с id={} не найдено", adId);
                        return new ResourceNotFoundException("Ad", "id", adId);
                    });
            chat.setAd(ad);
            chat.addParticipant(ad.getSeller());
            log.debug("Добавлен продавец id={} как участник чата", ad.getSeller().getId());
        }
        if (userId != null) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с id={} не найден", userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            chat.addParticipant(user);
            log.debug("Добавлен пользователь id={} как участник чата", userId);
        }
    }
}