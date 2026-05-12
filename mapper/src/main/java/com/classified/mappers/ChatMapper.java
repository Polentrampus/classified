package com.classified.mappers;

import com.classified.dto.chat.ChatCreateRequest;
import com.classified.dto.chat.ChatResponse;
import com.classified.entity.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Chat toEntity(ChatCreateRequest request);

    @Mapping(target = "adId", source = "ad.id")
    @Mapping(target = "createdAt", source = "createdAt")
    ChatResponse toResponse(Chat chat);
}
