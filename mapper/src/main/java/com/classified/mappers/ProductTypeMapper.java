package com.classified.mappers;

import com.classified.dto.adType.ProductTypeResponse;
import com.classified.entity.ProductType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductTypeMapper {

    ProductTypeResponse toResponse(ProductType productType);

    default ProductType toEntity(String name) {
        ProductType productType = new ProductType();
        productType.setName(name);
        return productType;
    }
}