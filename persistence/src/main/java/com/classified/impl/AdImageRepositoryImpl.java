package com.classified.impl;

import com.classified.entity.AdImage;
import com.classified.repository.AdImageRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdImageRepositoryImpl extends AbstractRepository<AdImage,Long> implements AdImageRepository {
    protected AdImageRepositoryImpl() {
        super(AdImage.class);
    }
}
