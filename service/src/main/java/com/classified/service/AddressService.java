package com.classified.service;

import com.classified.dto.address.AddressCreateRequest;
import com.classified.dto.address.AddressResponse;
import com.classified.entity.Address;
import com.classified.entity.City;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AddressMapper;
import com.classified.repository.AddressRepository;
import com.classified.repository.CityRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
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
        log.info("Создание адреса для userId={}", userDetails.getId());
        log.debug("Детали запроса: {}", request);

        Address address = addressMapper.toEntity(request);
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> {
                        log.warn("Город с id={} не найден", request.getCityId());
                        return new ResourceNotFoundException("City", "id", request.getCityId());
                    });
            address.setCity(city);
        }
        User user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userDetails.getId());
                    return new ResourceNotFoundException("User", "id", userDetails.getId());
                });
        address.setUser(user);

        AddressResponse response = addressMapper.toResponse(addressRepository.save(address));
        log.info("Адрес создан: id={}, userId={}, cityId={}", response.getId(), response.getUserId(), response.getCityId());
        return response;
    }

    public AddressResponse getById(Long id) {
        log.debug("Запрос адреса по id={}", id);
        AddressResponse response = addressMapper.toResponse(addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Адрес с id={} не найден", id);
                    return new ResourceNotFoundException("Address", "id", id);
                }));
        log.debug("Адрес найден: {}", response);
        return response;
    }

    public List<AddressResponse> getByUserId(Long userId) {
        log.debug("Запрос адресов для userId={}", userId);
        List<AddressResponse> addresses = addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
        log.debug("Найдено {} адресов для userId={}", addresses.size(), userId);
        return addresses;
    }

    public List<AddressResponse> getByCityId(Long cityId) {
        log.debug("Запрос адресов для cityId={}", cityId);
        List<AddressResponse> addresses = addressRepository.findByCityId(cityId).stream()
                .map(addressMapper::toResponse)
                .toList();
        log.debug("Найдено {} адресов для cityId={}", addresses.size(), cityId);
        return addresses;
    }

    @Transactional
    public void delete(Long addressId, UserDetailsImpl userDetails) {
        log.info("Удаление адреса id={} пользователем id={}", addressId, userDetails.getId());

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Адрес с id={} не найден", addressId);
                    return new ResourceNotFoundException("Address", "id", addressId);
                });
        Long userId = address.getUser().getId();
        if (userId.equals(userDetails.getId())) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с id={} не найден", userId);
                        return new ResourceNotFoundException("User", "id", userId);
                    });
            Set<Address> addressSet = user.getAddress();
            addressSet.remove(address);
            user.setAddress(addressSet);
            addressRepository.delete(address);
            log.info("Адрес id={} успешно удалён", addressId);
        } else {
            log.warn("Отказ в доступе: пользователь id={} пытался удалить адрес id={}, принадлежащий userId={}",
                    userDetails.getId(), addressId, userId);
            throw new AccessDeniedException("You can only edit your own ads");
        }
    }
}