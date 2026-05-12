package com.classified.mappers;

import com.classified.dto.message.MessageUpdateRequest;
import com.classified.dto.message.MessageCreateRequest;
import com.classified.dto.message.MessageResponse;
import com.classified.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    // MessageCreateRequest (DTO) → Message (Entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)         // устанавливается в сервисе
    @Mapping(target = "sender", ignore = true)       // устанавливается в сервисе
    @Mapping(target = "createdAt", ignore = true)    // Hibernate
    Message toEntity(MessageCreateRequest request);

    // Message (Entity) → MessageResponse (DTO)
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "createdAt", source = "createdAt")
    MessageResponse toResponse(Message message);

    // MessageUpdateRequest (DTO) → Message (Entity) (in-place)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(MessageUpdateRequest request, @MappingTarget Message message);
}
