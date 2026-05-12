package com.classified.service;

import com.classified.dto.user.UserRegistrationRequest;
import com.classified.dto.user.UserResponse;
import com.classified.dto.user.UserStatisticsDto;
import com.classified.dto.user.UserUpdateRequest;
import com.classified.entity.User;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.exception.technical.TechnicalException;
import lombok.RequiredArgsConstructor;
import com.classified.mappers.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.classified.repository.RoleRepository;
import com.classified.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(UserRegistrationRequest request){
        log.info("Регистрация нового пользователя: email={}", request.getEmail());
        log.debug("Данные регистрации: name={}, phone={}", request.getName(), request.getPhone());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email уже занят: {}", request.getEmail());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "User with email " + request.getEmail() + " already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            log.warn("Телефон уже занят: {}", request.getPhone());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "User with phone " + request.getPhone() + " already exists");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Роль USER не найдена в БД");
                    return new RuntimeException("Роль USER не найдена");
                }));
        userRepository.save(user);
        UserResponse response = userMapper.toResponse(user);
        log.info("Пользователь зарегистрирован: id={}, email={}, роль={}", response.getId(), response.getEmail(), response.getRole());
        return response;
    }

    @Transactional
    public UserResponse createAdmin(UserRegistrationRequest request){
        log.info("Создание администратора: email={}", request.getEmail());
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> {
                    log.error("Роль ADMIN не найдена в БД");
                    return new TechnicalException(ErrorCode.INTERNAL_ERROR,
                            "Default role ROLE_ADMIN not found in database");
                }));
        userRepository.save(user);
        UserResponse response = userMapper.toResponse(user);
        log.info("Администратор создан: id={}, email={}", response.getId(), response.getEmail());
        return response;
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request){
        log.info("Обновление профиля пользователя id={}", userId);
        log.debug("Новые данные: name={}, email={}, phone={}", request.getName(), request.getEmail(), request.getPhone());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });
        userMapper.updateEntityFromRequest(request, user);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.debug("Пароль изменён для пользователя id={}", userId);
        }
        UserResponse response = userMapper.toResponse(user);
        log.info("Профиль пользователя id={} обновлён", userId);
        return response;
    }

    public UserResponse getProfile(Long userId){
        log.debug("Запрос профиля пользователя id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });
        UserResponse response = userMapper.toResponse(user);
        log.debug("Профиль получен: email={}, роль={}", response.getEmail(), response.getRole());
        return response;
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword){
        log.info("Смена пароля для пользователя id={}", userId);

        if(Objects.equals(oldPassword, newPassword)) {
            log.warn("Попытка сменить пароль на идентичный для userId={}", userId);
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Новое и старое пароли совпадают");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });
        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            log.warn("Неверный текущий пароль для userId={}", userId);
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Неверный текущий пароль");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Пароль изменён для пользователя id={}", userId);
    }

    public UserStatisticsDto getUserStatistics(Long userId){
        log.debug("Запрос статистики для пользователя id={}", userId);
        UserStatisticsDto stats = userRepository.getUserStatistics(userId);
        log.debug("Статистика получена для userId={}: rating={}, totalAds={}",
                userId, stats != null ? stats.getRating() : "null", stats != null ? stats.getTotalAds() : "null");
        return stats;
    }

    public Double getUserRating(Long userId){
        log.debug("Запрос рейтинга пользователя id={}", userId);
        Double rating = userRepository.getRatingUser(userId);
        log.debug("Рейтинг userId={}: {}", userId, rating);
        return rating;
    }
}