package classified.repository.impl;

import classified.entity.AdCategory;
import classified.repository.AbstractRepository;
import classified.repository.AdCategoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdCategoryRepositoryImpl extends AbstractRepository<AdCategory,Long> implements AdCategoryRepository {
    protected AdCategoryRepositoryImpl() {
        super(AdCategory.class);
    }
}
