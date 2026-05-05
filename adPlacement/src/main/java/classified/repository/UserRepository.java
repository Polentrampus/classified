package classified.repository;

import classified.dto.user.UserStatisticsDto;
import classified.entity.Address;
import classified.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    UserStatisticsDto getUserStatistics(Long id); // использовать представление user_statistics
    Double getRatingUser(Long id);
}
