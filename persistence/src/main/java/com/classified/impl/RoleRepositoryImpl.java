package com.classified.impl;

import com.classified.entity.Role;
import org.springframework.stereotype.Repository;
import com.classified.repository.RoleRepository;

import java.util.Optional;

@Repository
public class RoleRepositoryImpl extends AbstractRepository<Role, Long> implements RoleRepository {
    protected RoleRepositoryImpl() {
        super(Role.class);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return executeWithResult("findByName", em -> {
            try {
                return Optional.of(
                        em.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                                .setParameter("name", name)
                                .getSingleResult()
                );
            } catch (Exception e) {
                return Optional.empty();
            }
        }, "name=" + name);
    }
}
