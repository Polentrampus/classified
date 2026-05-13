package com.classified.mappers;

import com.classified.dto.adType.AdCategoryResponse;
import com.classified.entity.AdCategory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdCategoryMapper {

    AdCategoryResponse toResponse(AdCategory category);

    default AdCategory toEntity(String name) {
        AdCategory category = new AdCategory();
        category.setName(name);
        return category;
    }
}