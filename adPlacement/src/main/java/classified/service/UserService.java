package classified.service;

import classified.dto.user.UserRegistrationRequest;
import classified.dto.user.UserResponse;
import classified.dto.user.UserStatisticsDto;
import classified.dto.user.UserUpdateRequest;
import classified.entity.User;
import classified.entity.mappers.UserMapper;
import classified.exception.ErrorCode;
import classified.exception.business.BusinessException;
import classified.exception.business.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import classified.repository.RoleRepository;
import classified.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // создаёт пользователя с ролью USER
    @Transactional
    public UserResponse register(UserRegistrationRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "User with email " + request.getEmail() + " already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "User with phone " + request.getPhone() + " already exists");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Роль USER не найдена")));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    // только для админа, создаёт пользователя с ролью ADMIN
    @Transactional
    public UserResponse createAdmin(UserRegistrationRequest request){
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Роль USER не найдена")));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userMapper.updateEntityFromRequest(request, user);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponse(user);
    }

    public UserResponse getProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword){
        if(Objects.equals(oldPassword, newPassword))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Новое и старое пароли совпадают");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Неверный текущий пароль");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    public UserStatisticsDto getUserStatistics(Long userId){
        return userRepository.getUserStatistics(userId);
    }
    public Double getUserRating(Long userId){
        return userRepository.getRatingUser(userId);
    }
}
