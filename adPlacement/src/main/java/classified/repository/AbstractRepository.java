package classified.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractRepository<T, ID> implements BaseRepository<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> entityClass;
    protected final Logger log;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.log = LoggerFactory.getLogger(getClass());
    }

    /** Для операций без возвращаемого значения (update, delete) */
    protected void execute(String operationName, Consumer<EntityManager> action, Object... context) {
        log.info("Выполнение операции: {} | Сущность: {} | Контекст: {}",
                operationName, entityClass.getSimpleName(), Arrays.toString(context));
        try {
            action.accept(entityManager);
            log.info("Операция {} успешно выполнена. Сущность: {} | Контекст: {}",
                    operationName, entityClass.getSimpleName(), Arrays.toString(context));
        } catch (Exception e) {
            log.error("Ошибка операции {}. Сущность: {} | Контекст: {}",
                    operationName, entityClass.getSimpleName(), Arrays.toString(context), e);
            throw e;
        }
    }

    /** Для операций с возвращаемым значением (find, save, exists) */
    protected <R> R executeWithResult(String operationName, Function<EntityManager, R> action, Object... context) {
        log.info("Выполнение операции: {} | Сущность: {} | Контекст: {}",
                operationName, entityClass.getSimpleName(), Arrays.toString(context));
        try {
            R result = action.apply(entityManager);
            log.info("Операция {} успешно выполнена. Сущность: {} | Результат: {}",
                    operationName, entityClass.getSimpleName(), summarizeResult(result));
            return result;
        } catch (Exception e) {
            log.error("Ошибка операции {}. Сущность: {} | Контекст: {}",
                    operationName, entityClass.getSimpleName(), Arrays.toString(context), e);
            throw e;
        }
    }

    /** Краткое описание результата для логирования (чтобы не засорять лог) */
    private String summarizeResult(Object result) {
        if (result == null) return "null";
        if (result instanceof Optional<?> opt) {
            return opt.isPresent() ? "present" : "empty";
        }
        if (result instanceof Boolean) return result.toString();
        if (result instanceof Number) return result.toString();

        return result.getClass().getSimpleName();
    }

    @Override
    public Optional<T> findById(ID id) {
        return executeWithResult("findById",
                em -> Optional.ofNullable(em.find(entityClass, id)),
                "id=" + id);
    }

    @Override
    // TODO надо сделать пагинацию, чтоб не тянуть все данные из БД
    public List<T> findAll() {
        return executeWithResult("findAll",
                em -> em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList(),
                "без параметров");

    }

    @Override
    public T save(T entity) {
        return executeWithResult("save",
                em -> {
                    em.persist(entity);
                    return entity;
                },
                "classified.entity=" + entity);
    }

    @Override
    public T update(T entity) {
        return executeWithResult("update",
                em -> em.merge(entity),
                "classified.entity=" + entity);
    }

    @Override
    public void delete(T entity) {
        execute("delete",
                em -> em.remove(em.contains(entity) ? entity : em.merge(entity)),
                "classified.entity=" + entity);
    }

    @Override
    public void deleteById(ID id) {
        execute("deleteById",
                em -> findById(id).ifPresent(this::delete),
                "id=" + id);
    }

    @Override
    public boolean existsById(ID id) {
        return executeWithResult("existsById",
                em -> findById(id).isPresent(),
                "id=" + id);
    }
}