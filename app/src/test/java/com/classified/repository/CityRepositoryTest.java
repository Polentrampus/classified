package com.classified.repository;

import com.classified.entity.City;
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
public class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private City city1;
    private City city2;

    @BeforeEach
    void setUp() {
        city1 = new City();
        city1.setName("Moscow");
        entityManager.persist(city1);

        city2 = new City();
        city2.setName("Saint Petersburg");
        entityManager.persist(city2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindCityById() {
        // when
        Optional<City> found = cityRepository.findById(city1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Moscow");
    }

    @Test
    void shouldReturnEmptyWhenCityNotFoundById() {
        // when
        Optional<City> found = cityRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllCities() {
        // when
        List<City> cities = cityRepository.findAll();

        // then
        assertThat(cities).hasSize(2);
        assertThat(cities).extracting(City::getName)
                .containsExactlyInAnyOrder("Moscow", "Saint Petersburg");
    }

    @Test
    void shouldSaveCity() {
        // given
        City newCity = new City();
        newCity.setName("Kazan");

        // when
        City saved = cityRepository.save(newCity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<City> found = cityRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Kazan");
    }

    @Test
    void shouldUpdateCity() {
        // given
        City managedCity = entityManager.find(City.class, city1.getId());
        String newName = "Moscow City";

        // when
        managedCity.setName(newName);
        cityRepository.update(managedCity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<City> found = cityRepository.findById(city1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(newName);
    }

    @Test
    void shouldDeleteCity() {
        // when
        City managedCity = entityManager.find(City.class, city2.getId());
        cityRepository.delete(managedCity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<City> found = cityRepository.findById(city2.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteCityById() {
        // when
        cityRepository.deleteById(city1.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<City> found = cityRepository.findById(city1.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckCityExistsById() {
        // when & then
        assertThat(cityRepository.existsById(city1.getId())).isTrue();
        assertThat(cityRepository.existsById(999L)).isFalse();
    }
}