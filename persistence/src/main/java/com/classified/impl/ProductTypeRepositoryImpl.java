package com.classified.impl;

import com.classified.entity.ProductType;
import org.springframework.stereotype.Repository;
import com.classified.repository.ProductTypeRepository;

import java.util.Optional;

@Repository
public class ProductTypeRepositoryImpl extends AbstractRepository<ProductType, Long> implements ProductTypeRepository {

    protected ProductTypeRepositoryImpl() {
        super(ProductType.class);
    }

    @Override
    public Optional<ProductType> findByName(String name) {
        return executeWithResult("findByName", em -> {
            try {
                return Optional.of(
                        em.createQuery("SELECT pt FROM ProductType pt WHERE pt.name = :name", ProductType.class)
                                .setParameter("name", name)
                                .getSingleResult()
                );
            } catch (Exception e) {
                return Optional.empty();
            }
        }, "name=" + name);
    }

    @Override
    public boolean existsByName(String name) {
        return executeWithResult("existsByName", em -> {
            Long count = em.createQuery(
                            "SELECT COUNT(pt) FROM ProductType pt WHERE pt.name = :name", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        }, "name=" + name);
    }
}