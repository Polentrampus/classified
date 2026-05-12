package com.classified.mappers;

import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.dto.message.MessageUpdateRequest;
import com.classified.entity.Chat;
import com.classified.entity.Message;
import com.classified.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public Message toEntity(MessageCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Message.MessageBuilder message = Message.builder();

        message.content( request.getContent() );

        return message.build();
    }

    @Override
    public MessageResponse toResponse(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageResponse.MessageResponseBuilder messageResponse = MessageResponse.builder();

        messageResponse.chatId( messageChatId( message ) );
        messageResponse.senderId( messageSenderId( message ) );
        messageResponse.createdAt( message.getCreatedAt() );
        messageResponse.id( message.getId() );
        messageResponse.content( message.getContent() );

        return messageResponse.build();
    }

    @Override
    public void updateEntityFromRequest(MessageUpdateRequest request, Message message) {
        if ( request == null ) {
            return;
        }

        message.setContent( request.getContent() );
    }

    private Long messageChatId(Message message) {
        if ( message == null ) {
            return null;
        }
        Chat chat = message.getChat();
        if ( chat == null ) {
            return null;
        }
        Long id = chat.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long messageSenderId(Message message) {
        if ( message == null ) {
            return null;
        }
        User sender = message.getSender();
        if ( sender == null ) {
            return null;
        }
        Long id = sender.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
