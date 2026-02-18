package com.example.bankcards.controller;

import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление пользователями (только ADMIN)")
public class UserController {

    private final UserService userService;

    @PostMapping("/{id}/disable")
    @Operation(
            summary = "Отключить пользователя (только ADMIN)",
            description = "Деактивирует пользователя. Заблокированный пользователь не сможет войти в систему."
    )
    public void disable(@PathVariable UUID id) {
        userService.disableUser(id);
    }

    @PostMapping("/{id}/enable")
    @Operation(
            summary = "Активировать пользователя (только ADMIN)",
            description = "Активирует ранее деактивированного пользователя."
    )
    public void enable(@PathVariable UUID id) {
        userService.enableUser(id);
    }
}
