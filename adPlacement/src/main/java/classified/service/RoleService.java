package classified.service;

import classified.dto.role.RoleResponse;
import classified.entity.Role;
import classified.entity.mappers.RoleMapper;
import classified.exception.business.ResourceNotFoundException;
import classified.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Transactional
    public RoleResponse create(String name) {
        return roleMapper.toResponse(roleRepository.save(roleMapper.toEntity(name)));
    }
    @Transactional
    public void delete(Long roleId) {
        roleRepository.delete(roleRepository
                .findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId)));
    }
    @Transactional
    public RoleResponse update(Long roleId, String name) {
        Role role = roleRepository
                .findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        role.setName(name);
        return roleMapper.toResponse(role);
    }

    public RoleResponse getRole(Long roleId) {
        return roleMapper.toResponse(roleRepository
                .findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId)));
    }

    public List<RoleResponse> getAllRole() {
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    public RoleResponse getRoleByName(String name){
        return roleMapper.toResponse(roleRepository
                .findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name)));
    }

}
