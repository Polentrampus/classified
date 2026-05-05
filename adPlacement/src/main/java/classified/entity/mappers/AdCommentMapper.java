package classified.entity.mappers;

import classified.dto.adComment.AdCommentCreateRequest;
import classified.dto.adComment.AdCommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import classified.entity.AdComment;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdCommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)  // установим в сервисе
    @Mapping(target = "createdAt", ignore = true)
    AdComment toEntity(AdCommentCreateRequest request);

    @Mapping(target = "orderId", source = "order.id")
    AdCommentResponse toResponse(AdComment comment);
}
