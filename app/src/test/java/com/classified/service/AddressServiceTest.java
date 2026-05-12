package com.classified.service;

import com.classified.dto.address.AddressCreateRequest;
import com.classified.dto.address.AddressResponse;
import com.classified.entity.Address;
import com.classified.entity.City;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.AddressMapper;
import com.classified.repository.AddressRepository;
import com.classified.repository.CityRepository;
import com.classified.repository.UserRepository;
import com.classified.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private User otherUser;
    private UserDetailsImpl userDetails;
    private UserDetailsImpl otherUserDetails;
    private City city;
    private Address address;
    private AddressCreateRequest createRequest;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(Role.builder().name("ROLE_USER").build())
                .address(new HashSet<>())
                .build();

        otherUser = User.builder()
                .id(2L)
                .role(Role.builder().name("ROLE_USER").build())
                .password("encoded")
                .email("other@test.com")
                .build();

        userDetails = new UserDetailsImpl(user);
        otherUserDetails = new UserDetailsImpl(otherUser);

        city = new City();
        city.setId(5L);
        city.setName("Moscow");

        createRequest = AddressCreateRequest.builder()
                .cityId(5L)
                .build();

        address = Address.builder()
                .id(10L)
                .user(user)
                .city(city)
                .build();

        addressResponse = AddressResponse.builder()
                .id(10L)
                .userId(1L)
                .cityId(5L)
                .build();
    }

    @Test
    void shouldCreateAddress() {
        when(addressMapper.toEntity(any(AddressCreateRequest.class))).thenReturn(address);
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toResponse(any(Address.class))).thenReturn(addressResponse);

        AddressResponse result = addressService.create(createRequest, userDetails);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCityId()).isEqualTo(5L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void shouldGetAddressById() {
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.getById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowExceptionWhenAddressNotFound() {
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAddressesByUserId() {
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(address));
        when(addressMapper.toResponse(any(Address.class))).thenReturn(addressResponse);

        List<AddressResponse> result = addressService.getByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetAddressesByCityId() {
        when(addressRepository.findByCityId(5L)).thenReturn(List.of(address));
        when(addressMapper.toResponse(any(Address.class))).thenReturn(addressResponse);

        List<AddressResponse> result = addressService.getByCityId(5L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeleteOwnAddress() {
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        addressService.delete(10L, userDetails);

        verify(addressRepository).delete(address);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingForeignAddress() {
        User foreignUser = User.builder().id(5L).build();
        Address foreignAddress = Address.builder().id(10L).user(foreignUser).city(city).build();
        when(addressRepository.findById(10L)).thenReturn(Optional.of(foreignAddress));

        assertThatThrownBy(() -> addressService.delete(10L, userDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(addressRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenAddressNotFoundForDelete() {
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.delete(999L, userDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}