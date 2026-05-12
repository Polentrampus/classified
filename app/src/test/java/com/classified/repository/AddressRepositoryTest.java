package com.classified.repository;

import com.classified.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BaseRepositoryTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private City city1;
    private City city2;
    private Address address1;
    private Address address2;
    private Address address3;

    @BeforeEach
    void setUp() {
        // Создаем роль
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        // Создаем пользователей
        user1 = User.builder()
                .name("User1")
                .lastName("Test")
                .email("user1@test.com")
                .phone("+79161111111")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .name("User2")
                .lastName("Test")
                .email("user2@test.com")
                .phone("+79162222222")
                .password("password123")
                .role(userRole)
                .build();
        entityManager.persist(user2);

        // Создаем города
        city1 = new City();
        city1.setName("Moscow");
        entityManager.persist(city1);

        city2 = new City();
        city2.setName("Saint Petersburg");
        entityManager.persist(city2);

        // Создаем адреса
        address1 = Address.builder()
                .user(user1)
                .city(city1)
                .build();
        entityManager.persist(address1);

        address2 = Address.builder()
                .user(user1)
                .city(city2)
                .build();
        entityManager.persist(address2);

        address3 = Address.builder()
                .user(user2)
                .city(city1)
                .build();
        entityManager.persist(address3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindAddressesByUserId() {
        // when
        List<Address> addresses = addressRepository.findByUserId(user1.getId());

        // then
        assertThat(addresses).hasSize(2);
        assertThat(addresses).allMatch(a -> a.getUser().getId().equals(user1.getId()));
    }

    @Test
    void shouldReturnEmptyListForNonExistentUserId() {
        // when
        List<Address> addresses = addressRepository.findByUserId(999L);

        // then
        assertThat(addresses).isEmpty();
    }

    @Test
    void shouldFindAddressesByCityId() {
        // when
        List<Address> addresses = addressRepository.findByCityId(city1.getId());

        // then
        assertThat(addresses).hasSize(2);
        assertThat(addresses).allMatch(a -> a.getCity().getId().equals(city1.getId()));
    }

    @Test
    void shouldReturnEmptyListForNonExistentCityId() {
        // when
        List<Address> addresses = addressRepository.findByCityId(999L);

        // then
        assertThat(addresses).isEmpty();
    }

    @Test
    void shouldSaveAddress() {
        // given
        Address newAddress = Address.builder()
                .user(user2)
                .city(city2)
                .build();

        // when
        Address saved = addressRepository.save(newAddress);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Address> found = addressRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user2.getId());
        assertThat(found.get().getCity().getId()).isEqualTo(city2.getId());
    }

    @Test
    void shouldFindAddressById() {
        // when
        Optional<Address> found = addressRepository.findById(address1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCity().getName()).isEqualTo("Moscow");
    }

    @Test
    void shouldReturnEmptyWhenAddressNotFoundById() {
        // when
        Optional<Address> found = addressRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllAddresses() {
        // when
        List<Address> addresses = addressRepository.findAll();

        // then
        assertThat(addresses).hasSize(3);
    }

    @Test
    void shouldUpdateAddress() {
        // given
        Address managedAddress = entityManager.find(Address.class, address1.getId());

        // when
        managedAddress.setCity(city2);
        addressRepository.update(managedAddress);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Address> found = addressRepository.findById(address1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCity().getId()).isEqualTo(city2.getId());
    }

    @Test
    void shouldDeleteAddress() {
        // when
        Address managedAddress = entityManager.find(Address.class, address3.getId());
        addressRepository.delete(managedAddress);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Address> found = addressRepository.findById(address3.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteAddressById() {
        // when
        addressRepository.deleteById(address2.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Address> found = addressRepository.findById(address2.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckAddressExistsById() {
        // when & then
        assertThat(addressRepository.existsById(address1.getId())).isTrue();
        assertThat(addressRepository.existsById(999L)).isFalse();
    }
}