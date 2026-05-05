package classified.repository;

import classified.entity.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
