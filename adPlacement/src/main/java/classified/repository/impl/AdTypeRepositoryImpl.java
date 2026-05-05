package classified.repository.impl;

import classified.entity.AdType;
import classified.repository.AbstractRepository;
import classified.repository.AdTypeRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdTypeRepositoryImpl extends AbstractRepository<AdType, Long> implements AdTypeRepository {
    protected AdTypeRepositoryImpl() {
        super(AdType.class);
    }
}
