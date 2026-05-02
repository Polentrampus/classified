package classified.repository.impl;

import classified.dto.UserStatisticsDto;
import classified.entity.User;
import classified.exception.business.ResourceNotFoundException;
import classified.exception.technical.DatabaseException;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import classified.repository.AbstractRepository;
import classified.repository.UserRepository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl extends AbstractRepository<User, Long> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return executeWithResult("findByEmail",
                em -> {
                    try {
                        return Optional.of(
                                em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                                        .setParameter("email", email)
                                        .getSingleResult()
                        );
                    } catch (NoResultException e) {
                        throw new ResourceNotFoundException("User", "email", email);
                    } catch (Exception e) {
                        throw new DatabaseException(email, e);
                    }
                },
                "email=" + email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return executeWithResult("existsByEmail",
                em -> {
                    Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                            .setParameter("email", email)
                            .getSingleResult();
                    return count > 0;
                },
                "email=" + email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return executeWithResult("existsByPhone",
                em -> {
                    Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.phone = :phone", Long.class)
                            .setParameter("phone", phone)
                            .getSingleResult();
                    return count > 0;
                },
                "phone=" + phone);
    }

    @Override
    public UserStatisticsDto getUserStatistics(Long user_id) {
        return executeWithResult("getUserStatistics",
                em -> {
                    try {
                        return (UserStatisticsDto) em.createNativeQuery("SELECT * FROM user_statistics WHERE user_id = :user_id", "UserStatisticsMapping")
                                .setParameter("user_id", user_id)
                                .getSingleResult();
                    } catch (NoResultException e) {
                        return null;
                    } catch (Exception e) {
                        throw new DatabaseException(user_id, e);
                    }
                },
                "id=" + user_id);
    }

    @Override
    public Double getRatingUser(Long id) {
        return executeWithResult("getRatingUser",
                em -> {
                    try {
                        return em.createQuery("SELECT us.rating FROM UserRating us WHERE us.user.id = :id", Double.class)
                                .setParameter("id", id)
                                .getSingleResult();
                    } catch (Exception e) {
                        throw new DatabaseException(id, e);
                    }
                },
                "id=" + id);
    }

}
