package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String username, String password, String roleName) {

        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("User already exists: {}", username);
            throw new IllegalArgumentException("User already exists: " + username);
        }

        Role role = roleService.getRoleByName(roleName);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("Created user: {} with role: {}", username, roleName);

        return savedUser;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });
    }

    @Transactional
    public void disableUser(UUID userId) {
        User user = findById(userId);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Disabled user: {}", user.getUsername());
    }

    @Transactional
    public void enableUser(UUID userId) {
        User user = findById(userId);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Enabled user: {}", user.getUsername());
    }
}
