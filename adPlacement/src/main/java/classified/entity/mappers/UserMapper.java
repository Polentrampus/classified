package classified.entity.mappers;

import classified.dto.user.UserRegistrationRequest;
import classified.dto.user.UserResponse;
import classified.dto.user.UserUpdateRequest;
import classified.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // 1. RegistrationRequest -> User
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)      // пароль кодируем в сервисе отдельно
    @Mapping(target = "role", ignore = true)          // роль установим в сервисе
    @Mapping(target = "createdAt", ignore = true)     // БД поставит
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRating", ignore = true)    // рейтинг создаётся отдельно
    User toEntity(UserRegistrationRequest request);

    // 2. User -> UserResponse
    @Mapping(target = "role", source = "role.name")             // user.getRole().getName()
    @Mapping(target = "rating", source = "userRating.rating", defaultValue = "0.00")   // user.getUserRating().getRating()
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRating", ignore = true)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

}