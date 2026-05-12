package com.classified.repository;

import com.classified.entity.Message;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

public interface MessageRepository extends BaseRepository<Message, Long> {
    PagedResult<Message> findByChatId(Long chatId, PagingRequest pageable);
    PagedResult<Message> findBySenderId(Long senderId, PagingRequest pageable);
}
