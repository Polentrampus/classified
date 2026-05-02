package classified.repository.impl;

import classified.entity.Message;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import classified.repository.AbstractRepository;
import classified.repository.MessageRepository;
import classified.util.pagination.PagedResult;
import classified.util.pagination.PagingRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepositoryImpl extends AbstractRepository<Message, Long> implements MessageRepository {
    protected MessageRepositoryImpl() {
        super(Message.class);
    }

    @Override
    /// Поиск сообщений по id чата. Пагинация для вывода только части последних сообщений
    public PagedResult<Message> findByChatId(Long chatId, PagingRequest pageable) {
        return executeWithResult("findByChatId", em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // Запросы данных
            CriteriaQuery<Message> query = cb.createQuery(Message.class);
            Root<Message> root = query.from(Message.class);
            query.select(root)
                    .where(cb.equal(root.get("chat").get("id"), chatId))
                    .orderBy(cb.desc(root.get("createdAt")));

            TypedQuery<Message> typed = em.createQuery(query);
            typed.setFirstResult((int) pageable.getOffset());
            typed.setMaxResults(pageable.getSize());
            List<Message> content = typed.getResultList();

            // Подсчёт общего количества
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Message> countRoot = countQuery.from(Message.class);
            countQuery.select(cb.count(countRoot))
                    .where(cb.equal(countRoot.get("chat").get("id"), chatId));
            Long total = em.createQuery(countQuery).getSingleResult();

            return new PagedResult<>(content, pageable.getPage(), pageable.getSize(), total);

        }, "chatId=" + chatId + "pageable=" + pageable);
    }

    @Override
    /// Поиск сообщений по id отправителя
    public PagedResult<Message> findBySenderId(Long senderId, PagingRequest pageable) {
        return executeWithResult("findBySenderId", em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // Запросы данных
            CriteriaQuery<Message> query = cb.createQuery(Message.class);
            Root<Message> root = query.from(Message.class);
            query.select(root)
                    .where(cb.equal(root.get("sender").get("id"), senderId))
                    .orderBy(cb.desc(root.get("createdAt")));

            TypedQuery<Message> typed = em.createQuery(query);
            typed.setFirstResult((int) pageable.getOffset());
            typed.setMaxResults(pageable.getSize());
            List<Message> content = typed.getResultList();

            // Подсчёт общего количества
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Message> countRoot = countQuery.from(Message.class);
            countQuery.select(cb.count(countRoot))
                    .where(cb.equal(countRoot.get("sender").get("id"), senderId));
            Long total = em.createQuery(countQuery).getSingleResult();

            return new PagedResult<>(content, pageable.getPage(), pageable.getSize(), total);

        }, "senderId=" + senderId + "pageable=" + pageable);
    }
}
