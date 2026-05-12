package com.classified.mappers;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.entity.Ad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

///  Чтобы самому прописывать исключения на поля, которые не мапим
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "adType", ignore = true)
    @Mapping(target = "images", ignore = true)
    Ad toEntity(AdCreateRequest request);

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "adTypeId", source = "adType.id")
    AdResponse toResponse(Ad ad);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "adType", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEntityFromRequest(AdUpdateRequest request, @MappingTarget Ad ad);
}