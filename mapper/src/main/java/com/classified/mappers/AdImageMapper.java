package com.classified.mappers;

import com.classified.dto.image.AdImageRequest;
import com.classified.dto.image.AdImageResponse;
import com.classified.entity.AdImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AdImage toEntity(AdImageRequest request);

    @Mapping(target = "adId", source = "ad.id")
    AdImageResponse toResponse(AdImage image);

    List<AdImageResponse> toResponseList(List<AdImage> images);
}