package com.classified.impl;

import com.classified.entity.AdType;
import org.springframework.stereotype.Repository;
import com.classified.repository.AdTypeRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdTypeRepositoryImpl extends AbstractRepository<AdType, Long> implements AdTypeRepository {

    protected AdTypeRepositoryImpl() {
        super(AdType.class);
    }

    @Override
    public List<AdType> findByCategoryId(Long categoryId) {
        return executeWithResult("findByCategoryId",
                em -> em.createQuery(
                                "SELECT at FROM AdType at JOIN FETCH at.type WHERE at.category.id = :categoryId", AdType.class)
                        .setParameter("categoryId", categoryId)
                        .getResultList(),
                "categoryId=" + categoryId);
    }

    @Override
    public List<AdType> findByTypeId(Long productTypeId) {
        return executeWithResult("findByTypeId",
                em -> em.createQuery(
                                "SELECT at FROM AdType at JOIN FETCH at.category WHERE at.type.id = :typeId", AdType.class)
                        .setParameter("typeId", productTypeId)
                        .getResultList(),
                "typeId=" + productTypeId);
    }

    @Override
    public Optional<AdType> findByTypeIdAndCategoryId(Long productTypeId, Long categoryId) {
        return executeWithResult("findByTypeIdAndCategoryId", em -> {
            try {
                return Optional.of(
                        em.createQuery(
                                        "SELECT at FROM AdType at WHERE at.type.id = :typeId AND at.category.id = :categoryId",
                                        AdType.class)
                                .setParameter("typeId", productTypeId)
                                .setParameter("categoryId", categoryId)
                                .getSingleResult()
                );
            } catch (Exception e) {
                return Optional.empty();
            }
        }, "typeId=" + productTypeId + ", categoryId=" + categoryId);
    }

    @Override
    public boolean existsByTypeIdAndCategoryId(Long productTypeId, Long categoryId) {
        return executeWithResult("existsByTypeIdAndCategoryId", em -> {
            Long count = em.createQuery(
                            "SELECT COUNT(at) FROM AdType at WHERE at.type.id = :typeId AND at.category.id = :categoryId",
                            Long.class)
                    .setParameter("typeId", productTypeId)
                    .setParameter("categoryId", categoryId)
                    .getSingleResult();
            return count > 0;
        }, "typeId=" + productTypeId + ", categoryId=" + categoryId);
    }
}