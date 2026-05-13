package com.classified.mappers;

import com.classified.dto.adType.AdTypeResponse;
import com.classified.entity.AdType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdTypeMapper {

    @Mapping(target = "productType", source = "type")
    @Mapping(target = "category", source = "category")
    AdTypeResponse toResponse(AdType adType);
}