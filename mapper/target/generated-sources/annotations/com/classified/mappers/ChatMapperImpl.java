package com.classified.mappers;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.entity.Ad;
import com.classified.entity.Chat;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class ChatMapperImpl implements ChatMapper {

    @Override
    public Chat toEntity(ChatCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Chat.ChatBuilder chat = Chat.builder();

        return chat.build();
    }

    @Override
    public ChatResponse toResponse(Chat chat) {
        if ( chat == null ) {
            return null;
        }

        ChatResponse.ChatResponseBuilder chatResponse = ChatResponse.builder();

        chatResponse.adId( chatAdId( chat ) );
        chatResponse.createdAt( chat.getCreatedAt() );
        chatResponse.id( chat.getId() );

        return chatResponse.build();
    }

    private Long chatAdId(Chat chat) {
        if ( chat == null ) {
            return null;
        }
        Ad ad = chat.getAd();
        if ( ad == null ) {
            return null;
        }
        Long id = ad.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
