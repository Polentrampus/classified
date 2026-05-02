package classified.entity.mappers;

import classified.dto.UserRegistrationRequest;
import classified.dto.UserResponse;
import classified.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    @Mapping(target = "rating", source = "userRating.rating")   // user.getUserRating().getRating()
    UserResponse toResponse(User user);
}