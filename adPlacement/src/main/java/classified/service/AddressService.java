package classified.service;

import classified.dto.address.AddressCreateRequest;
import classified.dto.address.AddressResponse;
import classified.entity.Address;
import classified.entity.City;
import classified.entity.mappers.AddressMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.AddressRepository;
import classified.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final AddressMapper addressMapper;

    @Transactional
    public AddressResponse create(AddressCreateRequest request) {
        Address address = addressMapper.toEntity(request);
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
            address.setCity(city);
        }
        return addressMapper.toResponse(addressRepository.save(address));
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
    public void delete(Long id) {
        addressRepository.delete(addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id)));
    }
}