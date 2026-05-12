package com.classified.mappers;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Promotion toEntity(PromotionCreateRequest request);

    @Mapping(target = "adId", source = "ad.id")
    PromotionResponse toResponse(Promotion promotion);
}