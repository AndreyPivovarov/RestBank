package com.example.bankcards.service;


import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", name);
                    return new RuntimeException("Role not found: " + name);
                });
    }

    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Role not found with ID: {}", id);
                    return new RuntimeException("Role not found with ID: " + id);
                });
    }
}
