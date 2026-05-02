package classified.entity.mappers;

import classified.dto.AdCreateRequest;
import classified.dto.AdResponse;
import classified.entity.Ad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "adType", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Ad toEntity(AdCreateRequest request);

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "adTypeId", source = "adType.id")
    AdResponse toResponse(Ad ad);
}