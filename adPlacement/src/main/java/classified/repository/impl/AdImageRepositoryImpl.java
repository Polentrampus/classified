package classified.repository.impl;

import classified.entity.AdImage;
import classified.repository.AbstractRepository;
import classified.repository.AdImageRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdImageRepositoryImpl extends AbstractRepository<AdImage,Long> implements AdImageRepository {
    protected AdImageRepositoryImpl() {
        super(AdImage.class);
    }
}
