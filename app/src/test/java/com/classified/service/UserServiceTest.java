package com.classified.service;

import com.classified.dto.user.UserRegistrationRequest;
import com.classified.dto.user.UserResponse;
import com.classified.dto.user.UserStatisticsDto;
import com.classified.dto.user.UserUpdateRequest;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.entity.UserRating;
import com.classified.exception.ErrorCode;
import com.classified.exception.business.BusinessException;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.UserMapper;
import com.classified.repository.RoleRepository;
import com.classified.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private User user;
    private Role userRole;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registrationRequest = UserRegistrationRequest.builder()
                .name("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+79161234567")
                .password("rawPassword123")
                .build();

        userRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        user = User.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+79161234567")
                .password("encodedPassword")
                .role(userRole)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+79161234567")
                .role("ROLE_USER")
                .rating(BigDecimal.ZERO)
                .build();
    }

    @Test
    void shouldRegisterNewUser() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegistrationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // when
        UserResponse result = userService.register(registrationRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");

        // Проверяем, что пароль был закодирован
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");

        verify(userRepository).existsByEmail("john@example.com");
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGetProfile() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // when
        UserResponse result = userService.getProfile(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getProfile(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateProfile() {
        // given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("+79169876543")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("+79169876543")
                .role(userRole)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .name("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("+79169876543")
                .role("ROLE_USER")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntityFromRequest(any(), any());
        when(userMapper.toResponse(any(User.class))).thenReturn(updatedResponse);

        // when
        UserResponse result = userService.updateProfile(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane");
        verify(userMapper).updateEntityFromRequest(updateRequest, user);
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatches() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        // when
        userService.changePassword(1L, "oldPassword", "newPassword");

        // then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordDoesNotMatch() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newPassword"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR)
                .hasMessageContaining("Неверный текущий пароль");
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordEqualsOld() {
        assertThatThrownBy(() -> userService.changePassword(1L, "samePassword", "samePassword"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR)
                .hasMessageContaining("Новое и старое пароли совпадают");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldCreateAdmin() {
        // given
        Role adminRole = Role.builder().id(2L).name("ROLE_ADMIN").build();
        when(userMapper.toEntity(any(UserRegistrationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // when
        UserResponse result = userService.createAdmin(registrationRequest);

        // then
        assertThat(result).isNotNull();
        verify(roleRepository).findByName("ROLE_ADMIN");
    }

    @Test
    void shouldGetUserStatistics() {
        // given
        UserStatisticsDto stats = new UserStatisticsDto(
                1L, "John", new BigDecimal("4.50"), 10L, 5L, new BigDecimal("5000.00"));
        when(userRepository.getUserStatistics(1L)).thenReturn(stats);

        // when
        UserStatisticsDto result = userService.getUserStatistics(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualByComparingTo("4.50");
        assertThat(result.getTotalAds()).isEqualTo(10L);
        assertThat(result.getTotalSales()).isEqualTo(5L);
    }

    @Test
    void shouldGetUserRating() {
        // given
        when(userRepository.getRatingUser(1L)).thenReturn(4.5);

        // when
        Double rating = userService.getUserRating(1L);

        // then
        assertThat(rating).isEqualTo(4.5);
        verify(userRepository).getRatingUser(1L);
    }
}