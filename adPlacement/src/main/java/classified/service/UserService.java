package classified.service;

import classified.dto.UserRegistrationRequest;
import classified.dto.UserResponse;
import classified.dto.UserStatisticsDto;
import classified.dto.UserUpdateRequest;
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
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // создаёт пользователя с ролью USER
    public UserResponse register(UserRegistrationRequest request){
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Роль USER не найдена")));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }
    // только для админа, создаёт пользователя с ролью ADMIN
    public UserResponse createAdmin(UserRegistrationRequest request){
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Роль USER не найдена")));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }
    public UserResponse updateProfile(Long userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse getProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }
    public void changePassword(Long userId, String oldPassword, String newPassword){
        if(Objects.equals(oldPassword, newPassword))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Новое и старое пароли совпадают");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }
    public UserStatisticsDto getUserStatistics(Long userId){
        return userRepository.getUserStatistics(userId);
    }
    public Double getUserRating(Long userId){
        return userRepository.getRatingUser(userId);
    }
}
