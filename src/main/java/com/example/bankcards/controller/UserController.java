package com.example.bankcards.controller;

import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class UserController {

    private final UserService userService;

    @PostMapping("/{id}/disable")
    public void disable(@PathVariable UUID id) {
        userService.disableUser(id);
    }

    @PostMapping("/{id}/enable")
    public void enable(@PathVariable UUID id) {
        userService.enableUser(id);
    }
}
