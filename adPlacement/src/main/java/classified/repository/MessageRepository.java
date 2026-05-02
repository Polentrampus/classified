package classified.repository;

import classified.entity.Message;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;

public interface MessageRepository extends BaseRepository<Message, Long> {
    PagedResult<Message> findByChatId(Long chatId, PagingRequest pageable);
    PagedResult<Message> findBySenderId(Long senderId, PagingRequest pageable);
}
