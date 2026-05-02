package classified.repository.impl;

import classified.entity.AdType;
import classified.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdTypeRepositoryImpl extends AbstractRepository<AdType, Long> {
    protected AdTypeRepositoryImpl() {
        super(AdType.class);
    }
}
