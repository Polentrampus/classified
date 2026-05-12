package com.classified.impl;

import com.classified.dto.ad.AdSearchCriteria;
import com.classified.entity.Ad;
import com.classified.entity.Promotion;
import com.classified.entity.User;
import com.classified.entity.UserRating;
import com.classified.exception.business.FieldSortingException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import com.classified.repository.AdRepository;
import com.classified.pagination.Direction;
import com.classified.pagination.PagedResult;
import com.classified.pagination.PagingRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class AdRepositoryImpl extends AbstractRepository<Ad, Long> implements AdRepository {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "price", "createdAt", "title", "status", "sellerRating"
    );

    protected AdRepositoryImpl() {
        super(Ad.class);
    }

    @Override
    public PagedResult<Ad> searchAds(AdSearchCriteria criteria, PagingRequest pageable) {
        return executeWithResult("searchAds", em -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Ad> query = cb.createQuery(Ad.class);
            Root<Ad> ad = query.from(Ad.class);

            Join<Ad, User> sellerJoin = ad.join("seller", JoinType.LEFT);

            List<Predicate> predicates = buildPredicates(criteria, cb, ad, sellerJoin);
            query.where(predicates.toArray(new Predicate[0]));

            // сортировка:
            if(pageable.getSort().isSorted()){
                query.orderBy(buildOrder(pageable, cb, ad, sellerJoin));
            } else {
                query.orderBy(cb.desc(ad.get("createdAt")));
            }

            // инициализируем запрос
            TypedQuery<Ad> typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult((int)pageable.getOffset());
            typedQuery.setMaxResults(pageable.getSize());

            List<Ad> content = typedQuery.getResultList();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Ad> countRoot = countQuery.from(Ad.class);

            Join<Ad, User> countSellerJoin = countRoot.join("seller", JoinType.LEFT);

            List<Predicate> countPredicates = buildPredicates(criteria, cb, countRoot, countSellerJoin);
            if (!countPredicates.isEmpty()) {
                countQuery.where(countPredicates.toArray(new Predicate[0]));
            }
            countQuery.select(cb.count(countRoot));
            Long total = em.createQuery(countQuery).getSingleResult();

            return new PagedResult<>(content, pageable.getPage(),pageable.getSize(), total);
        }, "criteria=" + criteria + ", pageable=" + pageable);
    }

    @Override
    public List<Ad> findBySellerId(Long sellerId) {
        return executeWithResult("findBySellerId",
                em -> em.createQuery("SELECT a FROM Ad a WHERE a.seller.id = :sellerId", Ad.class)
                        .setParameter("sellerId", sellerId)
                        .getResultList(),
                "sellerId=" + sellerId);
    }

    private List<Predicate> buildPredicates(AdSearchCriteria criteria,
                                            CriteriaBuilder cb,
                                            Root<Ad> adRoot,
                                            Join<Ad, User> sellerJoin) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getTitle() != null && !criteria.getTitle().isBlank()) {
            predicates.add(cb.like(cb.lower(adRoot.get("title")),
                    "%" + criteria.getTitle().toLowerCase() + "%"));
        }
        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(adRoot.get("price"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(adRoot.get("price"), criteria.getMaxPrice()));
        }
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(adRoot.get("status"), criteria.getStatus()));
        }
        if (criteria.getSellerId() != null) {
            predicates.add(cb.equal(sellerJoin.get("id"), criteria.getSellerId()));
        }
        if (criteria.getMinSellerRating() != null) {
            Join<User, UserRating> ratingJoin = sellerJoin.join("userRating", JoinType.LEFT);

            predicates.add(cb.greaterThanOrEqualTo(ratingJoin.get("rating"),
                    criteria.getMinSellerRating()));
        }

        return predicates;
    }

    private List<Order> buildOrder(PagingRequest pageable,
                                   CriteriaBuilder cb,
                                   Root<Ad> adRoot,
                                   Join<Ad, User> sellerJoin){
        // Создали список правил сортировки
        List<Order> orders = new ArrayList<>();
        Join<User, UserRating> ratingJoin = sellerJoin.join("userRating", JoinType.LEFT);
        Join<Ad, Promotion> promotionJoin = adRoot.join("promotions", JoinType.LEFT);
        // Добавляем условия к join
        promotionJoin.on(
                cb.and(
                        cb.isTrue(promotionJoin.get("isActive")),
                        cb.greaterThan(promotionJoin.get("endDate"), cb.currentTimestamp())
                )
        );

        // Сначала добавляем сортировку по промо (всегда первое)
        orders.add(cb.asc(cb.selectCase()
                .when(cb.isNotNull(promotionJoin.get("id")), 0)
                .otherwise(1)));

        pageable.getSort().getOrders().forEach(sortOrder -> {
            String property = sortOrder.field();
            // проверяем: поля сортировки те, по которым можно сортировать?
            if (!ALLOWED_SORT_FIELDS.contains(property)) {
                throw new FieldSortingException();
            }
            Path<Object> path;
            if ("sellerRating".equals(property)) {
                path = ratingJoin.get("rating");
            } else {
                path = adRoot.get(property);
            }

            orders.add(sortOrder.direction() == Direction.ASC ? cb.asc(path) : cb.desc(path));        });
        return orders;
    }
}
