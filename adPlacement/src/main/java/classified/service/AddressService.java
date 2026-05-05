package classified.service;

import classified.dto.address.AddressCreateRequest;
import classified.dto.address.AddressResponse;
import classified.entity.Ad;
import classified.entity.Address;
import classified.entity.City;
import classified.entity.User;
import classified.entity.mappers.AddressMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AddressRepository;
import classified.repository.CityRepository;
import classified.repository.UserRepository;
import classified.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse create(AddressCreateRequest request, UserDetailsImpl userDetails) {
        if (request.getUserId().equals(userDetails.getId())) {
            Address address = addressMapper.toEntity(request);
            if (request.getCityId() != null) {
                City city = cityRepository.findById(request.getCityId())
                        .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
                address.setCity(city);
            }
            User user = userRepository
                    .findById(request
                            .getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
            address.setUser(user);
            addressRepository.save(address);

            return addressMapper.toResponse(addressRepository.save(address));
        } else
            throw new AccessDeniedException("You can only edit your own ads");
    }

    public AddressResponse getById(Long id) {
        return addressMapper.toResponse(addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id)));
    }

    public List<AddressResponse> getByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    public List<AddressResponse> getByCityId(Long cityId) {
        return addressRepository.findByCityId(cityId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long addressId, UserDetailsImpl userDetails) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        Long userId = address.getUser().getId();
        if (userId.equals(userDetails.getId())) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Set<Address> addressSet = user.getAddress();
            addressSet.remove(address);
            user.setAddress(addressSet);
            addressRepository.delete(address);
        } else
            throw new AccessDeniedException("You can only edit your own ads");
    }
}