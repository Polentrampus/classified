package classified.entity.mappers;

import classified.dto.role.RoleResponse;
import classified.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    // RoleCreateRequest -> Role
    default Role toEntity(String name){
        return Role.builder().name(name).build();
    }
    // Role -> Response
    RoleResponse toResponse(Role role);
}
