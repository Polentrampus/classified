package com.classified.impl;
import com.classified.entity.AdCategory;
import com.classified.repository.AdCategoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdCategoryRepositoryImpl extends AbstractRepository<AdCategory,Long> implements AdCategoryRepository {
    protected AdCategoryRepositoryImpl() {
        super(AdCategory.class);
    }
}
