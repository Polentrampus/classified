package classified.service;

import classified.dto.chat.ChatCreateRequest;
import classified.dto.chat.ChatResponse;
import classified.entity.Ad;
import classified.entity.Address;
import classified.entity.Chat;
import classified.entity.User;
import classified.entity.mappers.ChatMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AdRepository;
import classified.repository.ChatRepository;
import classified.repository.UserRepository;
import classified.security.UserDetailsImpl;
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
        Optional<Chat> existing = chatRepository.findByAdIdAndBuyerId(request.getAdId(), request.getUserId());
        if (existing.isPresent()) {
            return chatMapper.toResponse(existing.get());
        }
        Chat chat = chatMapper.toEntity(request);
        if(!request.getUserId().equals(userDetails.getId()))
            throw new AccessDeniedException("You can only edit your own ads");
        updateRelatedEntities(request.getAdId(), request.getUserId(), chat);
        return chatMapper.toResponse(chatRepository.save(chat));
    }

    @Transactional
    public void delete(Long chatId) {
        chatRepository.delete(chatRepository
                .findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId)));
    }

    public ChatResponse getChat(Long chatId) {
        return chatMapper.toResponse(chatRepository
                .findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId)));
    }

    public List<ChatResponse> getAllChats() {
        return chatRepository.findAll().stream().map(chatMapper::toResponse).toList();
    }

    public ChatResponse findByAdId(Long adId) {
        return chatMapper
                .toResponse(chatRepository
                        .findByAdId(adId)
                        .orElseThrow(() -> new ResourceNotFoundException("Chat", "adId", adId)));
    }

    public List<ChatResponse> findByUserId(Long userId) {
        List<Chat> chats = chatRepository.findByUserId(userId);
        return chats.stream().map(chatMapper::toResponse).toList();
    }

    public ChatResponse findByAdIdAndBuyerId(Long adId, Long buyerId) {
        return chatMapper
                .toResponse(chatRepository
                        .findByAdIdAndBuyerId(adId, buyerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Chat",
                                        "adId and buyerId",
                                        adId, buyerId)));
    }

    private void updateRelatedEntities(Long adId, Long userId, Chat chat) {
        if (adId != null) {
            Ad ad = adRepository
                    .findById(adId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", adId));
            chat.setAd(ad);
            chat.addParticipant(ad.getSeller());
        }
        if (userId != null) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            chat.addParticipant(user);
        }
    }
}
