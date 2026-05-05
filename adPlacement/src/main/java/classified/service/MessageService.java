package classified.service;

import classified.dto.message.MessageCreateRequest;
import classified.dto.message.MessageResponse;
import classified.dto.message.MessageUpdateRequest;
import classified.entity.Chat;
import classified.entity.Message;
import classified.entity.User;
import classified.entity.mappers.MessageMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.ChatRepository;
import classified.repository.MessageRepository;
import classified.repository.UserRepository;
import classified.security.UserDetailsImpl;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Message message = messageMapper.toEntity(request);
        if(!message.getSender().getId().equals(userDetails.getId()))
            throw new AccessDeniedException("You can only edit your own ads");
        updateRelatedEntities(request.getChatId(), request.getSenderId(),message);
        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Transactional
    public void delete(Long messageId, UserDetailsImpl userDetails) {
        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        if(!message.getSender().getId().equals(userDetails.getId()))
            throw new AccessDeniedException("You can only edit your own ads");
        messageRepository.delete(message);
    }

    @Transactional
    public MessageResponse update(MessageUpdateRequest request, Long messageId, UserDetailsImpl userDetails) {
        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        if(!message.getSender().getId().equals(userDetails.getId()))
            throw new AccessDeniedException("You can only edit your own ads");
        messageMapper.updateEntityFromRequest(request, message);
        return messageMapper.toResponse(message);
    }

    public MessageResponse getMessage(Long messageId) {
        return messageMapper.toResponse(messageRepository
                .findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId)));
    }

    public List<MessageResponse> getAllMessage() {
        return messageRepository.findAll().stream().map(messageMapper::toResponse).toList();
    }

    public PagedResult<MessageResponse> findByChatId(Long chatId, PagingRequest pageable){
        PagedResult<Message> messagePagedResult = messageRepository.findByChatId(chatId, pageable);

        List<MessageResponse> content = messagePagedResult
                .getContent()
                .stream()
                .map(messageMapper::toResponse)
                .toList();

        return new PagedResult<>(content,
                messagePagedResult.getPage(),
                messagePagedResult.getSize(),
                messagePagedResult.getTotalElements());
    }

    public PagedResult<MessageResponse> findBySenderId(Long senderId, PagingRequest pageable){
        PagedResult<Message> messagePagedResult = messageRepository.findBySenderId(senderId, pageable);

        List<MessageResponse> content = messagePagedResult
                .getContent()
                .stream()
                .map(messageMapper::toResponse)
                .toList();

        return new PagedResult<>(content,
                messagePagedResult.getPage(),
                messagePagedResult.getSize(),
                messagePagedResult.getTotalElements());

    }

    private void updateRelatedEntities(Long chatId, Long senderId, Message message) {
        if (chatId != null) {
            Chat chat = chatRepository
                    .findById(chatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));
            message.setChat(chat);
        }
        if (senderId != null) {
            User sender = userRepository
                    .findById(senderId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
            message.setSender(sender);
        }
    }
}
