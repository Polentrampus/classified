package classified.repository;

import classified.entity.Address;

import java.util.List;

public interface AddressRepository extends BaseRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    List<Address> findByCityId(Long cityId);
}
