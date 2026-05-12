package com.classified.impl;

import com.classified.entity.City;
import org.springframework.stereotype.Repository;
import com.classified.repository.CityRepository;

@Repository
public class CityRepositoryImpl extends AbstractRepository<City, Long> implements CityRepository {
    protected CityRepositoryImpl() {
        super(City.class);
    }
}
