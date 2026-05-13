package com.classified.repository;

import com.classified.entity.ProductType;

import java.util.Optional;

public interface ProductTypeRepository extends BaseRepository<ProductType, Long> {
    Optional<ProductType> findByName(String name);
    boolean existsByName(String name);
}