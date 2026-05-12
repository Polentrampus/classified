package com.classified.impl;

import com.classified.entity.Address;
import org.springframework.stereotype.Repository;
import com.classified.repository.AddressRepository;

import java.util.List;

@Repository
public class AddressRepositoryImpl extends AbstractRepository<Address, Long> implements AddressRepository {
    protected AddressRepositoryImpl() {
        super(Address.class);
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        return executeWithResult("findByUserId",
                em -> em.createQuery("SELECT a FROM Address a WHERE a.user.id = :userId", Address.class)
                        .setParameter("userId", userId)
                        .getResultList(),
                "userId=" + userId);
    }

    @Override
    public List<Address> findByCityId(Long cityId) {
        return executeWithResult("findByCityId",
                em -> em.createQuery("SELECT a FROM Address a WHERE a.city.id = :cityId", Address.class)
                        .setParameter("cityId", cityId)
                        .getResultList(),
                "cityId=" + cityId);
    }
}
