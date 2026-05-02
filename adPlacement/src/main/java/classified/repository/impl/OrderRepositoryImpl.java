package classified.repository.impl;

import classified.entity.Order;
import classified.entity.OrderStatus;
import classified.repository.AbstractRepository;
import classified.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order, Long> implements OrderRepository {
    protected OrderRepositoryImpl() {
        super(Order.class);
    }

    @Override
    public List<Order> findByAdId(Long adId) {
        return executeWithResult("findByAdId", em ->
                em.createQuery("SELECT o FROM Order o WHERE o.ad.id = :adId", Order.class)
                        .setParameter("adId", adId)
                        .getResultList(), "adId=" + adId);
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        return executeWithResult("findByBuyerId", em ->
                em.createQuery("SELECT o FROM Order o WHERE o.buyer.id = :buyerId", Order.class)
                        .setParameter("buyerId", buyerId)
                        .getResultList(), "buyerId=" + buyerId);
    }

    @Override
    public List<Order> findBySellerId(Long sellerId) {
        return executeWithResult("findBySellerId", em ->
                em.createQuery("SELECT o FROM Order o WHERE o.ad.seller.id = :sellerId", Order.class)
                        .setParameter("sellerId", sellerId)
                        .getResultList(), "sellerId=" + sellerId);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return executeWithResult("findByStatus", em ->
           em.createQuery("SELECT o from Order o WHERE o.status = :status", Order.class)
                   .setParameter("status", status)
                   .getResultList(),"status=" + status);
    }
}
