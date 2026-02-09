package com.example.bankcards.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String role
) {
}
