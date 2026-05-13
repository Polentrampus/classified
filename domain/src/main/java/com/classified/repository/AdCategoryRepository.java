package com.classified.repository;

import com.classified.entity.AdCategory;

import java.util.Optional;

public interface AdCategoryRepository extends BaseRepository<AdCategory,Long>{
    Optional<AdCategory> findByName(String name);
    boolean existsByName(String name);
}
