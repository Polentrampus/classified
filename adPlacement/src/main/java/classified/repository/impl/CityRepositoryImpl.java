package classified.repository.impl;

import classified.entity.City;
import classified.repository.AbstractRepository;
import classified.repository.ChatRepository;
import classified.repository.CityRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CityRepositoryImpl extends AbstractRepository<City, Long> implements CityRepository {
    protected CityRepositoryImpl() {
        super(City.class);
    }
}
