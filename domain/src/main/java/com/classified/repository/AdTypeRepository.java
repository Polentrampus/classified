package com.classified.repository;

import com.classified.entity.AdType;
import com.classified.entity.ProductType;

import java.util.List;
import java.util.Optional;

public interface AdTypeRepository extends BaseRepository<AdType,Long>{
    List<AdType> findByCategoryId(Long categoryId);
    List<AdType> findByTypeId(Long productTypeId);
    Optional<AdType> findByTypeIdAndCategoryId(Long productTypeId, Long categoryId);
    boolean existsByTypeIdAndCategoryId(Long productTypeId, Long categoryId);
}
