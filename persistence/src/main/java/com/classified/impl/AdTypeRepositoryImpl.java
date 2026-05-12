package com.classified.impl;

import com.classified.entity.AdType;
import org.springframework.stereotype.Repository;
import com.classified.repository.AdTypeRepository;

@Repository
public class AdTypeRepositoryImpl extends AbstractRepository<AdType, Long> implements AdTypeRepository {
    protected AdTypeRepositoryImpl() {
        super(AdType.class);
    }
}
