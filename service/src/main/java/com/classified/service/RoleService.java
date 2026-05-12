package com.classified.service;

import com.classified.dto.role.RoleResponse;
import com.classified.entity.Role;
import com.classified.exception.business.ResourceNotFoundException;
import com.classified.mappers.RoleMapper;
import com.classified.repository.RoleRepository;
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
        log.info("Создание роли: name={}", name);
        RoleResponse response = roleMapper.toResponse(roleRepository.save(roleMapper.toEntity(name)));
        log.info("Роль создана: id={}, name={}", response.getId(), response.getName());
        return response;
    }

    @Transactional
    public void delete(Long roleId) {
        log.info("Удаление роли id={}", roleId);
        roleRepository.delete(roleRepository
                .findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Роль с id={} не найдена", roleId);
                    return new ResourceNotFoundException("Role", "id", roleId);
                }));
        log.info("Роль id={} удалена", roleId);
    }

    @Transactional
    public RoleResponse update(Long roleId, String name) {
        log.info("Обновление роли id={}, новое имя={}", roleId, name);
        Role role = roleRepository
                .findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Роль с id={} не найдена", roleId);
                    return new ResourceNotFoundException("Role", "id", roleId);
                });
        role.setName(name);
        RoleResponse response = roleMapper.toResponse(role);
        log.info("Роль обновлена: id={}, name={}", response.getId(), response.getName());
        return response;
    }

    public RoleResponse getRole(Long roleId) {
        log.debug("Запрос роли по id={}", roleId);
        RoleResponse response = roleMapper.toResponse(roleRepository
                .findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Роль с id={} не найдена", roleId);
                    return new ResourceNotFoundException("Role", "id", roleId);
                }));
        log.debug("Роль найдена: name={}", response.getName());
        return response;
    }

    public List<RoleResponse> getAllRole() {
        log.debug("Запрос всех ролей");
        List<RoleResponse> roles = roleRepository.findAll().stream()
                .map(roleMapper::toResponse).toList();
        log.debug("Найдено {} ролей", roles.size());
        return roles;
    }

    public RoleResponse getRoleByName(String name){
        log.debug("Поиск роли по имени={}", name);
        RoleResponse response = roleMapper.toResponse(roleRepository
                .findByName(name)
                .orElseThrow(() -> {
                    log.warn("Роль с именем={} не найдена", name);
                    return new ResourceNotFoundException("Role", "name", name);
                }));
        log.debug("Роль найдена: id={}", response.getId());
        return response;
    }
}