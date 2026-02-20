package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Аутентификация и регистрация пользователей")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с ролью USER. Username должен быть уникальным."
    )
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid AuthRequestDto request) {

        User user = userService.createUser(request.username(), request.password(), "ROLE_USER");

        UserResponseDto body = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole().getName()
        );

        return ResponseEntity
                .created(URI.create("/users " + user.getId()))
                .body(body);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя. Возвращает JWT токен для доступа к защищенным эндпоинтам."
    )
    public AuthResponseDto login(@RequestBody @Valid AuthRequestDto request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        List<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        String token = jwtService.generateToken(request.username(), roles);

        return new AuthResponseDto(token);
    }
}
